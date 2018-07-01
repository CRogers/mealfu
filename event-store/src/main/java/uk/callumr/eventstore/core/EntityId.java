package uk.callumr.eventstore.core;

import org.immutables.value.Value;

import java.util.UUID;

@Value.Immutable
public abstract class EntityId {
    public abstract String asString();

    public static EntityId of(String id) {
        return ImmutableEntityId.builder()
                .asString(id)
                .build();
    }

    public static EntityId random() {
        return EntityId.of(UUID.randomUUID().toString());
    }
}
