package uk.callumr.eventstore.core;

import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
public abstract class EventFilter3 {
    public abstract Set<EntityId> entityIds();
    public abstract Set<EventType> eventTypes();

    @Value.Check
    protected void check() {
        Preconditions.checkArgument(!entityIds().isEmpty(), "At least one entityId must be specified");
    }

    static class Builder extends ImmutableEventFilter3.Builder { }

    static Builder builder() {
        return new Builder();
    }

    public static EventFilter3 forEntities(EntityId entityId, EntityId... entityIds) {
        return builder()
                .addEntityIds(entityId)
                .addEntityIds(entityIds)
                .build();
    }

    public static EventFilter3 forEntity(EntityId entityId) {
        return forEntities(entityId);
    }

    public EventFilter3 ofTypes(EventType... eventTypes) {
        return builder()
                .from(this)
                .addEventTypes(eventTypes)
                .build();
    }

    public EventFilter3 ofType(EventType eventType) {
        return ofTypes(eventType);
    }
}

