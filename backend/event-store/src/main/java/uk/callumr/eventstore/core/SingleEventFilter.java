package uk.callumr.eventstore.core;

import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
public abstract class SingleEventFilter {
    protected abstract EntityId entityId();
    protected abstract Set<EventType> eventTypes();

    public EventFilter3 toMultiFilter() {
        return EventFilter3.builder()
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
}
