package uk.callumr.eventstore.core;

import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
public interface EntityIdAnd<V> extends Map.Entry<EntityId, V> {
    EntityId getKey();
    V getValue();

    @Override
    default V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    static <V> EntityIdAnd<V> of(EntityId entityId, V value) {
        return ImmutableEntityIdAnd.<V>builder()
                .key(entityId)
                .value(value)
                .build();
    }
}
