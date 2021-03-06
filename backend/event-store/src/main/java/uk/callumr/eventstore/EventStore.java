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
        return events(EventFilter.forEntity(entityId).ofTypes(eventTypes));
    }

    default SingleEvents events(SingleEventFilter singleEventFilter) {
        Events events = events(singleEventFilter.toMultiFilter());

        Stream<Event> eventStream = events
                .eventStreams()
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseGet(Stream::empty);

        return SingleEvents.builder()
                .events(eventStream)
                .eventTokenSupplier(events::eventToken)
                .build();
    }

    Events events(EventFilter eventFilter);

    default void withEvents(SingleEventFilter singleEventFilter, Function<Stream<Event>, Stream<Event>> projectionFunc) {
        withEvents(singleEventFilter.toMultiFilter(), manyEventStreams -> {
            Stream<Event> events = manyEventStreams.findFirst()
                    .map(Map.Entry::getValue)
                    .orElseGet(Stream::empty);

            return projectionFunc.apply(events);
        });
    }

    void withEvents(EventFilter eventFilter, Function<EntryStream<EntityId, Stream<Event>>, Stream<Event>> projectionFunc);
}
