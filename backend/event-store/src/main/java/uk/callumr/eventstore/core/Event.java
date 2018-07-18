package uk.callumr.eventstore.core;

public interface Event {
    EntityId entityId();
    EventType eventType();
    String data();

    static BasicEvent.Builder builder() {
        return BasicEvent.builder();
    }

    static Event of(EntityId entityId, EventType eventType, String data) {
        return builder()
                .entityId(entityId)
                .eventType(eventType)
                .data(data)
                .build();
    }
}
