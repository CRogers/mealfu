package uk.callumr.eventstore;

import uk.callumr.eventstore.core.*;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

public interface EventStore {
    void addEvents(Stream<Event> events);

    default void addEvents(Event... events) {
        addEvents(Arrays.stream(events));
    }

    Stream<VersionedEvent> events(EventFilters filters);

    default Events events(EventFilter2 eventFilter2) {
        throw new RuntimeException();
    }

    void withEvents(EventFilters filters, Function<Stream<VersionedEvent>, Stream<Event>> projectionFunc);

}
