package uk.callumr.eventstore;

import uk.callumr.eventstore.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class InMemoryEventStore implements EventStore {
    private AtomicLong version;
    private List<VersionedEvent> events;

    public InMemoryEventStore() {
        clear();
    }

    @Override
    public void clear() {
        version = new AtomicLong(0);
        events = new ArrayList<>();
    }

    @Override
    public void addEvent(Event event) {
        events.add(VersionedEvent.builder()
                .version(version.getAndIncrement())
                .event(BasicEvent.builder()
                        .entityId(event.entityId())
                        .eventType(event.eventType())
                        .data(event.data())
                        .build())
                .build()
        );
    }

    @Override
    public Stream<VersionedEvent> events(EventFilters filters) {
        Predicate<Event> eventPredicate = filters.stream().reduce(
                event -> false,
                (predicate, eventFilter) -> predicate.or(EventFilter.caseOf(eventFilter)
                        .forEntity(eventValueEqualTo(Event::entityId))
                        .ofType(eventValueEqualTo(Event::eventType))
                        .all(() -> event -> true)),
                Predicate::or);

        return events.stream()
                .filter(versionedEvent -> eventPredicate.test(versionedEvent.event()));
    }

    @Override
    public void withEvents(EventFilters filters, Function<Stream<VersionedEvent>, Stream<Event>> projectionFunc) {
        projectionFunc.apply(events(filters))
                .forEach(this::addEvent);
    }

    private static <T> Function<T, Predicate<Event>> eventValueEqualTo(Function<Event, T> extractor) {
        return value -> event -> value.equals(extractor.apply(event));
    }
}
