package uk.callumr.eventstore;

import one.util.streamex.EntryStream;
import uk.callumr.eventstore.core.*;
import uk.callumr.eventstore.core.internal.SingleEvents;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public interface EventStore {
    void addEvents(Stream<Event> events);

    default void addEvents(Event... events) {
        addEvents(Arrays.stream(events));
    }

    default SingleEvents eventsFor(EntityId entityId, EventType... eventTypes) {
        Events events = events(EventFilter3.forEntity(entityId).ofTypes(eventTypes));

        Stream<Event> eventStream = events
                .eventStreams()
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseGet(Stream::empty);

        return SingleEvents.builder()
                .events(eventStream)
                .eventToken(events.eventToken())
                .build();
    }

    Events events(EventFilter3 eventFilter);

    void withEvents(EventFilters filters, Function<Stream<VersionedEvent>, Stream<Event>> projectionFunc);

    void withEvents(EventFilter3 eventFilter, Function<EntryStream<EntityId, Event>, Stream<Event>> projectionFunc);
}
