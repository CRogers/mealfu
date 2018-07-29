package uk.callumr.eventstore.core;

import org.immutables.value.Value;

@Value.Immutable
public abstract class EventType {
    public abstract String asString();

    public static EventType of(String eventType) {
        return ImmutableEventType.builder()
                .asString(eventType)
                .build();
    }

    public Event newEvent(EntityId entityId, String data) {
        return BasicEvent.builder()
                .entityId(entityId)
                .eventType(this)
                .data(data)
                .build();
    }
}
