package uk.callumr.eventstore.core;

import org.immutables.value.Value;

@Value.Immutable
public interface BasicEvent extends Event {
    class Builder extends ImmutableBasicEvent.Builder {}

    static Builder builder() {
        return new Builder();
    }
}
