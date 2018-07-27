package uk.callumr.eventstore.core;

import org.immutables.value.Value;

@Value.Immutable
public abstract class BasicEventType {

    public static BasicEventType of(String eventType) {
        return ImmutableBasicEventType.builder()
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

    public abstract String asString();
}
