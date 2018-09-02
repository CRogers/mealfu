package uk.callumr.eventstore.core;

import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;

@Value.Immutable
public abstract class EventFilter {
    public abstract Set<EntityId> entityIds();
    public abstract Set<EventType> eventTypes();
    protected abstract Optional<EventToken> sinceEventToken();

    @Value.Check
    protected void check() {
        Preconditions.checkArgument(!entityIds().isEmpty(), "At least one entityId must be specified");
    }

    public static class Builder extends ImmutableEventFilter.Builder { }

    public static Builder builder() {
        return new Builder();
    }

    public static EventFilter forEntities(EntityId entityId, EntityId... entityIds) {
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

    public EventFilter ofTypes(EventType... eventTypes) {
        return builder()
                .from(this)
                .addEventTypes(eventTypes)
                .build();
    }

    public EventFilter ofType(EventType eventType) {
        return ofTypes(eventType);
    }

    public EventFilter since(Optional<EventToken> eventToken) {
        return builder()
                .from(this)
                .sinceEventToken(eventToken)
                .build();
    }

    public <T> T toCondition(
            BinaryOperator<T> and,
            Function<Set<EntityId>, T> entityIdsCondition,
            Function<Set<EventType>, T> eventTypesCondition,
            Function<EventToken, T> sinceEventTokenCondition) {

        List<T> conditions = new ArrayList<>(3);
        if (!entityIds().isEmpty()) {
            conditions.add(entityIdsCondition.apply(entityIds()));
        }
        if (!eventTypes().isEmpty()) {
            conditions.add(eventTypesCondition.apply(eventTypes()));
        }
        sinceEventToken().ifPresent(eventToken -> {
            conditions.add(sinceEventTokenCondition.apply(eventToken));
        });
        return conditions.stream()
                .reduce(and)
                .orElseThrow(() -> new IllegalArgumentException("Filters must contain at least one entity id"));
    }
}

