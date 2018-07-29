package uk.callumr.eventstore.core;

import org.immutables.value.Value;

@Value.Immutable
public interface Event {
    EntityId entityId();
    EventType eventType();
    String data();

    class Builder extends ImmutableEvent.Builder {}

    static Event.Builder builder() {
        return new Event.Builder();
    }

    static Event of(EntityId entityId, EventType eventType, String data) {
        return builder()
                .entityId(entityId)
                .eventType(eventType)
                .data(data)
                .build();
    }
}
