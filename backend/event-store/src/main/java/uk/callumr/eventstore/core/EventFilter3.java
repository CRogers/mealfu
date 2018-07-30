package uk.callumr.eventstore.core;

import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
public interface EventFilter3 {
    Set<EntityId> entityIds();
    Set<EventType> eventTypes();

    class Builder extends ImmutableEventFilter3.Builder { }

    static Builder builder() {
        return new Builder();
    }

    default EventFilter3 ofTypes(EventType... eventTypes) {
        return builder()
                .from(this)
                .addEventTypes(eventTypes)
                .build();
    }

    default EventFilter3 ofType(EventType eventType) {
        return ofTypes(eventType);
    }
}

