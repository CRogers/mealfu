package uk.callumr.eventstore.core;

import org.immutables.value.Value;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Value.Immutable
public abstract class Events {
    public abstract EventToken eventToken();
    protected abstract Map<EntityId, Stream<Event>> eventStreams();

    public Stream<Event> eventsFor(EntityId entityId) {
        return Optional.ofNullable(eventStreams().get(entityId))
                .orElseThrow(() -> new IllegalArgumentException(entityId + " does not have a corresponding event stream"));
    }
}
