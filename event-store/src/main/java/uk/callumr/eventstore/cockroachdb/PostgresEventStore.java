package uk.callumr.eventstore.cockroachdb;

import org.jooq.*;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.core.*;
import uk.callumr.eventstore.jooq.JooqUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

public class PostgresEventStore implements EventStore {
    private static final DataType<Long> SERIAL = new DefaultDataType<>(SQLDialect.POSTGRES, Long.class, "serial").nullable(false);
    private static final Field<Long> VERSION = DSL.field("version", SERIAL);
    private static final Field<String> ENTITY_ID = DSL.field("entityId", SQLDataType.VARCHAR.nullable(false));
    private static final Field<String> EVENT_TYPE = DSL.field("eventType", SQLDataType.VARCHAR.nullable(false));
    private static final Field<String> DATA = DSL.field("data", SQLDataType.VARCHAR.nullable(false));

    private final Table<Record> eventsTable;
    private final DSLContext jooq;

    public PostgresEventStore(ConnectionProvider connectionProvider, String schema) {
        this.jooq = DSL.using(connectionProvider, SQLDialect.POSTGRES);
        this.eventsTable = DSL.table(schema + ".events");
    }

    @Override
    public void clear() {
        createEventsTable();
        createIndex();
    }

    private void createEventsTable() {
        logSQL(jooq.createTable(eventsTable)
                .column(VERSION)
                .column(ENTITY_ID)
                .column(EVENT_TYPE)
                .column(DATA)
                .constraint(DSL.primaryKey(VERSION))).execute();

    }

    private void createIndex() {
        logSQL(jooq.createIndexIfNotExists("entityId")
                .on(eventsTable, ENTITY_ID, VERSION))
                .execute();
    }

    @Override
    public void addEvent(Event event) {
        jooq.transaction(configuration -> insertEvents(DSL.using(configuration), Stream.of(event)));
    }

    @Override
    public Stream<VersionedEvent> events(EventFilters filters) {
        Condition condition = eventFiltersToCondition(filters);

        return jooq.transactionResult(configuration -> {
            return logSQL(DSL.using(configuration)
                    .select(VERSION, ENTITY_ID, EVENT_TYPE, DATA)
                    .from(eventsTable)
                    .where(condition))
                    .stream()
                    .map(this::toVersionedEvent);
        });
    }

    @Override
    public void withEvents(EventFilters filters, Function<Stream<VersionedEvent>, Stream<Event>> projectionFunc) {
        Condition condition = eventFiltersToCondition(filters);

        Stream<VersionedEvent> events = jooq.transactionResult(configuration -> {
            DSLContext dsl = DSL.using(configuration);

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

        int addedRows = jooq.transactionResult(configuration -> {
            DSLContext dsl = DSL.using(configuration);

            return logSQL(dsl.insertInto(eventsTable)
                    .columns(ENTITY_ID, EVENT_TYPE, DATA)
                    .select(this.<Record3<String, String, String>>selectStar(dsl)
                            .from(values)
                            .whereNotExists(dsl
                                    .selectOne()
                                    .from(eventsTable)
                                    .where(versionSearch))))
                    .execute();
        });

        System.out.println("addedRows = " + addedRows);
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

    private <T extends Query> T logSQL(T query) {
        System.out.println(query.getSQL(ParamType.INLINED));
        return query;
    }

    private InsertValuesStep3<Record, String, String, String> insertEvent(InsertValuesStep3<Record, String, String, String> iv, Event event) {
        return iv.values(event.entityId().asString(), event.eventType().asString(), event.data());
    }

    private Condition eventFiltersToCondition(EventFilters filters) {
        EventFilter eventFilter = filters.stream()
                .findFirst()
                .get();

        return EventFilter.caseOf(eventFilter)
                .forEntity(entityId -> ENTITY_ID.equal(entityId.asString()))
                .ofType(eventType -> EVENT_TYPE.equal(eventType.asString()))
                .all(DSL::trueCondition);
    }

    private VersionedEvent toVersionedEvent(Record4<Long, String, String, String> record) {
        return VersionedEvent.builder()
                .version(record.component1())
                .event(BasicEvent.builder()
                        .entityId(EntityId.of(record.component2()))
                        .eventType(EventType.of(record.component3()))
                        .data(record.component4())
                        .build())
                .build();
    }

    static {
        JooqUtils.noLogo();
    }
}
