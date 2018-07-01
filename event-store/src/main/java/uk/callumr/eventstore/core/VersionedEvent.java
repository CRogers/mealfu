package uk.callumr.eventstore.core;

import org.immutables.value.Value;

@Value.Immutable
public interface VersionedEvent {
    long version();
    Event event();



    static ImmutableVersionedEvent.Builder builder() {
        return ImmutableVersionedEvent.builder();
    }
}
