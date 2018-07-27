package uk.callumr.eventstore;

import uk.callumr.eventstore.core.Event;
import uk.callumr.eventstore.core.EventFilters;
import uk.callumr.eventstore.core.VersionedEvent;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

public interface EventStore {
    void addEvents(Stream<Event> events);

    default void addEvents(Event... events) {
        addEvents(Arrays.stream(events));
    }

    Stream<VersionedEvent> events(EventFilters filters);

    void withEvents(EventFilters filters, Function<Stream<VersionedEvent>, Stream<Event>> projectionFunc);

}
