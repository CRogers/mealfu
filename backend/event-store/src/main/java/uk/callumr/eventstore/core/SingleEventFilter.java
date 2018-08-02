package uk.callumr.eventstore.core;

import org.immutables.value.Value;

import java.util.Optional;
import java.util.Set;

@Value.Immutable
public abstract class SingleEventFilter {
    protected abstract EntityId entityId();
    protected abstract Set<EventType> eventTypes();
    protected abstract Optional<EventToken> sinceEventToken();

    public EventFilter toMultiFilter() {
        return EventFilter.builder()
                .addEntityIds(entityId())
                .eventTypes(eventTypes())
                .build();
    }

    protected static class Builder extends ImmutableSingleEventFilter.Builder { }

    protected static Builder builder() {
        return new Builder();
    }

    public SingleEventFilter ofTypes(EventType... eventTypes) {
        return builder()
                .from(this)
                .addEventTypes(eventTypes)
                .build();
    }

    public SingleEventFilter ofType(EventType eventType) {
        return ofTypes(eventType);
    }

    public SingleEventFilter since(Optional<EventToken> eventToken) {
        return builder()
                .from(this)
                .sinceEventToken(eventToken)
                .build();
    }
}
