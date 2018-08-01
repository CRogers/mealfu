package uk.callumr.eventstore.cockroachdb;

import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.google.common.base.Suppliers;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.core.*;
import uk.callumr.eventstore.core.internal.EventId;
import uk.callumr.eventstore.jooq.JooqUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PostgresEventStore implements EventStore {
    private static final Logger log = LoggerFactory.getLogger(PostgresEventStore.class);

    private static final DataType<Long> SERIAL = new DefaultDataType<>(SQLDialect.POSTGRES, Long.class, "serial").nullable(false);
    private static final Field<Long> VERSION = DSL.field("version", SERIAL);
    private static final Field<String> ENTITY_ID = DSL.field("entityId", SQLDataType.VARCHAR.nullable(false));
    private static final Field<String> EVENT_TYPE = DSL.field("eventType", SQLDataType.VARCHAR.nullable(false));
    private static final Field<String> DATA = DSL.field("data", SQLDataType.VARCHAR.nullable(false));
    private static final int MAX_TRIES = 10;

    private final Table<Record> eventsTable;
    private final Supplier<DSLContext> jooq;

    public PostgresEventStore(ConnectionProvider connectionProvider, String schema) {
        this.eventsTable = DSL.table(schema + ".events");
        DSLContext dslContext = DSL.using(connectionProvider, SQLDialect.POSTGRES);
        this.jooq = Suppliers.memoize(() -> {
            createTablesUnlessExists(eventsTable, dslContext);
            return dslContext;
        });
    }

    private DSLContext jooq() {
        return jooq.get();
    }

    private static void createTablesUnlessExists(Table<Record> eventsTable, DSLContext dslContext) {
        logSQL(dslContext.createTableIfNotExists(eventsTable)
                .column(VERSION)
                .column(ENTITY_ID)
                .column(EVENT_TYPE)
                .column(DATA)
                .constraint(DSL.primaryKey(VERSION))).execute();

        logSQL(dslContext.createIndexIfNotExists("entityId")
                .on(eventsTable, ENTITY_ID, VERSION))
                .execute();
    }

    @Override
    public void addEvents(Stream<Event> events) {
        transaction(dsl -> insertEvents(dsl, events));
    }

    @Override
    public Stream<VersionedEvent> events(EventFilters filters) {
        Condition condition = eventFiltersToCondition(filters);

        return transactionResult(dsl -> logSQL(dsl
                .select(VERSION, ENTITY_ID, EVENT_TYPE, DATA)
                .from(eventsTable)
                .where(condition))
                .stream()
                .map(this::toVersionedEvent));
    }

    @Override
    public Events events(EventFilter2 eventFilters) {
        Condition condition = eventFiltersToCondition2(eventFilters);

        Stream<Event> eventStream = transactionResult(dsl -> logSQL(dsl
                .select(VERSION, ENTITY_ID, EVENT_TYPE, DATA)
                .from(eventsTable)
                .where(condition))
                .stream()
                .map(this::toVersionedEvent))
                .map(VersionedEvent::event);

        return Events.builder()
                .consecutiveEventStreams(eventStream)
                .eventToken(EventToken.of(EventId.of(-999)))
                .build();
    }

    @Override
    public void withEvents(EventFilters filters, Function<Stream<VersionedEvent>, Stream<Event>> projectionFunc) {
        Condition condition = eventFiltersToCondition(filters);

        new CallExecutor<>(new RetryConfigBuilder()
                .withMaxNumberOfTries(MAX_TRIES)
                .withNoWaitBackoff()
                .withDelayBetweenTries(Duration.ZERO)
                .retryOnReturnValue(0)
                .build())
                .execute(() -> {
                    int addedRows = withEventsInner(condition, projectionFunc);
                    log.debug("addedRows = {}", addedRows);
                    return addedRows;
                });
    }

    private int withEventsInner(Condition condition, Function<Stream<VersionedEvent>, Stream<Event>> projectionFunc) {
        Stream<VersionedEvent> events = transactionResult(dsl -> {
            return logSQL(dsl
                    .select(VERSION, ENTITY_ID, EVENT_TYPE, DATA)
                    .from(eventsTable)
                    .where(condition))
                    .stream()
                    .map(this::toVersionedEvent);
        });

        AtomicReference<Optional<Long>> lastVersion = new AtomicReference<>(Optional.empty());

        Stream<Event> apply = projectionFunc.apply(events
                .peek(event -> lastVersion.set(Optional.of(event.version()))));

        Condition versionSearch = lastVersion.get()
                .map(VERSION::greaterThan)
                .orElse(DSL.trueCondition());

        Event event = apply.findFirst().get();
        Table<Record3<String, String, String>> values = DSL.values(
                DSL.row(event.entityId().asString(), event.eventType().asString(), event.data()));

        return transactionResult(dsl -> {
            return logSQL(dsl.insertInto(eventsTable)
                    .columns(ENTITY_ID, EVENT_TYPE, DATA)
                    .select(this.<Record3<String, String, String>>selectStar(dsl)
                            .from(values)
                            .whereNotExists(dsl
                                    .selectOne()
                                    .from(eventsTable)
                                    .where(versionSearch)
                                    .and(condition))))
                    .execute();
        });
    }

    private <R> R transactionResult(Function<DSLContext, R> func) {
        return jooq().transactionResult(configuration -> {
            DSLContext dsl = DSL.using(configuration);
            logSQL(dsl.query("set transaction isolation level serializable")).execute();
            return func.apply(dsl);
        });
    }

    private void transaction(Consumer<DSLContext> consumer) {
        transactionResult(dsl -> {
            consumer.accept(dsl);
            return null;
        });
    }

    private <T extends Record> SelectSelectStep<T> selectStar(DSLContext dsl) {
        return (SelectSelectStep<T>) dsl
                        .select(DSL.field("*"));
    }

    private void insertEvents(DSLContext dsl, Stream<Event> events) {
        logSQL(events
                .reduce(
                        dsl.insertInto(eventsTable).columns(ENTITY_ID, EVENT_TYPE, DATA),
                        this::insertEvent,
                        throwErrorOnParallelCombine()))
                .execute();
    }

    private <T> BinaryOperator<T> throwErrorOnParallelCombine() {
        return (a, b) -> {
            throw new RuntimeException();
        };
    }

    private static <T extends Query> T logSQL(T query) {
//        log.debug(query.getSQL(ParamType.INLINED));
        return query;
    }

    private InsertValuesStep3<Record, String, String, String> insertEvent(InsertValuesStep3<Record, String, String, String> iv, Event event) {
        return iv.values(event.entityId().asString(), event.eventType().asString(), event.data());
    }

    private Condition eventFiltersToCondition2(EventFilter2 eventFilters) {
        return eventFilters.filters().stream()
                .map(eventFilter -> {
                    List<Condition> conditions = new ArrayList<>(2);
                    if (!eventFilter.entityIds().isEmpty()) {
                        conditions.add(ENTITY_ID.in(eventFilter.entityIds().stream()
                                .map(EntityId::asString)
                                .collect(Collectors.toList())));
                    }
                    if (!eventFilter.eventTypes().isEmpty()) {
                        conditions.add(EVENT_TYPE.in(eventFilter.eventTypes().stream()
                                .map(EventType::asString)
                                .collect(Collectors.toList())));
                    }
                    return conditions.stream()
                            .reduce(Condition::and)
                            .orElseThrow(() -> new IllegalArgumentException("Filters must contain at least one entity id"));
                })
                .reduce(Condition::or)
                .orElseThrow(() -> new IllegalArgumentException("Must narrow search to at least one filter"));
    }

    private Condition eventFiltersToCondition(EventFilters filters) {
        EventFilter eventFilter = filters.stream()
                .findFirst()
                .get();

        return EventFilter.caseOf(eventFilter)
                .forEntity(entityId -> ENTITY_ID.equal(entityId.asString()))
                .ofType(eventType -> EVENT_TYPE.equal(eventType.asString()));
    }

    private VersionedEvent toVersionedEvent(Record4<Long, String, String, String> record) {
        return VersionedEvent.builder()
                .version(record.component1())
                .event(Event.builder()
                        .entityId(BasicEntityId.of(record.component2()))
                        .eventType(EventType.of(record.component3()))
                        .data(record.component4())
                        .build())
                .build();
    }

    static {
        JooqUtils.noLogo();
    }
}
