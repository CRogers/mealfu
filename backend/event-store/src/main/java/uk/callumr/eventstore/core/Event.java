package uk.callumr.eventstore.core;

public interface Event {
    EntityId entityId();
    BasicEventType eventType();
    String data();

    static BasicEvent.Builder builder() {
        return BasicEvent.builder();
    }

    static Event of(EntityId entityId, BasicEventType eventType, String data) {
        return builder()
                .entityId(entityId)
                .eventType(eventType)
                .data(data)
                .build();
    }
}
