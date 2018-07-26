package uk.callumr.eventstore.core;

import org.immutables.value.Value;

import java.util.UUID;

@Value.Immutable
public abstract class BasicEntityId implements EntityId {

    public static BasicEntityId of(String id) {
        return ImmutableBasicEntityId.builder()
                .asString(id)
                .build();
    }

    public static BasicEntityId random() {
        return BasicEntityId.of(UUID.randomUUID().toString());
    }
}
