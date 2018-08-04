package uk.callumr.eventstore.cockroachdb;

import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.google.common.base.Suppliers;
import one.util.streamex.EntryStream;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.core.*;
import uk.callumr.eventstore.core.internal.EventId;
import uk.callumr.eventstore.core.internal.ReadableReducingStream;
import uk.callumr.eventstore.jooq.JooqUtils;

import java.time.Duration;
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
        dslContext.createTableIfNotExists(eventsTable)
                .column(VERSION)
                .column(ENTITY_ID)
                .column(EVENT_TYPE)
                .column(DATA)
                .constraint(DSL.primaryKey(VERSION))
                .execute();

        dslContext.createIndexIfNotExists("entityId")
                .on(eventsTable, ENTITY_ID, VERSION)
                .execute();
    }

    @Override
    public void addEvents(Stream<Event> events) {
        transaction(dsl -> insertEvents(dsl, events));
    }

    @Override
    public Events events(EventFilter eventFilter) {
        Condition condition = eventFiltersToCondition2(eventFilter);

        ReadableReducingStream<VersionedEvent, Long> events = new ReadableReducingStream<>(
                eventsForCondition(condition),
                0L,
                (maxVersion, versionedEvent) -> versionedEvent.version());

        return Events.builder()
                .consecutiveEventStreams(events.stream().map(VersionedEvent::event))
                .eventTokenSupplier(() -> events.reduction().map(EventId::of).map(EventToken::of))
                .build();
    }

    private Stream<VersionedEvent> eventsForCondition(Condition condition) {
        return transactionResult(dsl -> dsl
                .select(VERSION, ENTITY_ID, EVENT_TYPE, DATA)
                .from(eventsTable)
                .where(condition))
                .stream()
                .map(this::toVersionedEvent);
    }

    @Override
    public void withEvents(EventFilter eventFilter, Function<EntryStream<EntityId, Stream<Event>>, Stream<Event>> projectionFunc) {
        Condition condition = eventFiltersToCondition2(eventFilter);

        new CallExecutor<>(new RetryConfigBuilder()
                .withMaxNumberOfTries(MAX_TRIES)
                .withNoWaitBackoff()
                .withDelayBetweenTries(Duration.ZERO)
                .retryOnReturnValue(0)
                .build())
                .execute(() -> {
                    return withEventsInner(condition, projectionFunc);
                });
    }

    private int withEventsInner(Condition condition, Function<EntryStream<EntityId, Stream<Event>>, Stream<Event>> projectionFunc) {
        Stream<VersionedEvent> events = eventsForCondition(condition);

        AtomicReference<Optional<Long>> lastVersion = new AtomicReference<>(Optional.empty());

        Stream<Event> apply = projectionFunc.apply(Events.consecutiveEventsToEntryStream(events
                .peek(event -> lastVersion.set(Optional.of(event.version())))
                .map(VersionedEvent::event)));

        Condition versionSearch = lastVersion.get()
                .map(VERSION::greaterThan)
                .orElse(DSL.trueCondition());

        Event event = apply.findFirst().get();
        Table<Record3<String, String, String>> values = DSL.values(
                DSL.row(event.entityId().asString(), event.eventType().asString(), event.data()));

        return transactionResult(dsl -> {
            return dsl.insertInto(eventsTable)
                    .columns(ENTITY_ID, EVENT_TYPE, DATA)
                    .select(this.<Record3<String, String, String>>selectStar(dsl)
                            .from(values)
                            .whereNotExists(dsl
                                    .selectOne()
                                    .from(eventsTable)
                                    .where(versionSearch)
                                    .and(condition)))
                    .execute();
        });
    }

    private <R> R transactionResult(Function<DSLContext, R> func) {
        return jooq().transactionResult(configuration -> {
            DSLContext dsl = DSL.using(configuration);
            dsl.query("set transaction isolation level serializable").execute();
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
        events
                .reduce(
                        dsl.insertInto(eventsTable).columns(ENTITY_ID, EVENT_TYPE, DATA),
                        this::insertEvent,
                        throwErrorOnParallelCombine())
                .execute();
    }

    private <T> BinaryOperator<T> throwErrorOnParallelCombine() {
        return (a, b) -> {
            throw new RuntimeException();
        };
    }

    private InsertValuesStep3<Record, String, String, String> insertEvent(InsertValuesStep3<Record, String, String, String> iv, Event event) {
        return iv.values(event.entityId().asString(), event.eventType().asString(), event.data());
    }

    private Condition eventFiltersToCondition2(EventFilter eventFilter) {
        return eventFilter.toCondition(
                Condition::and,
                entityIds -> ENTITY_ID.in(entityIds.stream()
                        .map(EntityId::asString)
                        .collect(Collectors.toList())),
                eventTypes -> EVENT_TYPE.in(eventTypes.stream()
                        .map(EventType::asString)
                        .collect(Collectors.toList())),
                eventToken -> VERSION.greaterThan(eventToken.lastEventAccessed().eventId()));
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
