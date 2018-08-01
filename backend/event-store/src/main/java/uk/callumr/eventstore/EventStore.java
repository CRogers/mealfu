package uk.callumr.eventstore;

import uk.callumr.eventstore.core.*;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public interface EventStore {
    void addEvents(Stream<Event> events);

    default void addEvents(Event... events) {
        addEvents(Arrays.stream(events));
    }

    Stream<VersionedEvent> events(EventFilters filters);

    default Stream<Event> eventsFor(EntityId entityId, EventType... eventTypes) {
        return events(EventFilter3.forEntity(entityId).ofTypes(eventTypes))
                .eventStreams()
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseGet(Stream::empty);
    }

    default Events events(EventFilter3 eventFilter, EventFilter3... eventFilters) {
        return events(EventFilter2.builder()
                .addFilters(eventFilter)
                .addFilters(eventFilters)
                .build());
    }

    default Events events(EventFilter2 eventFilters) {
        throw new UnsupportedOperationException();
    }

    void withEvents(EventFilters filters, Function<Stream<VersionedEvent>, Stream<Event>> projectionFunc);

}
