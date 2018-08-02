package uk.callumr.eventstore.core;

import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;

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

    public static SingleEventFilter forEntity(EntityId entityId) {
        return SingleEventFilter.builder()
                .entityId(entityId)
                .build();
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

    public <T> T toCondition(
            BinaryOperator<T> and,
            Function<Set<EntityId>, T> entityIdsCondition,
            Function<Set<EventType>, T> eventTypesCondition) {

        List<T> conditions = new ArrayList<>(2);
        if (!entityIds().isEmpty()) {
            conditions.add(entityIdsCondition.apply(entityIds()));
        }
        if (!eventTypes().isEmpty()) {
            conditions.add(eventTypesCondition.apply(eventTypes()));
        }
        return conditions.stream()
                .reduce(and)
                .orElseThrow(() -> new IllegalArgumentException("Filters must contain at least one entity id"));
    }
}

