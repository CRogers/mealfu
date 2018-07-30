package uk.callumr.eventstore.core;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class EventFilter2 {
    public abstract List<EventFilter3> filters();

    static class Builder extends ImmutableEventFilter2.Builder { }

    static EventFilter3 forEntities(EntityId... entityIds) {
        return EventFilter3.builder()
                .addEntityIds(entityIds)
                .build();
    }

    static EventFilter3 forEntity(EntityId entityId) {
        return forEntities(entityId);
    }
}
