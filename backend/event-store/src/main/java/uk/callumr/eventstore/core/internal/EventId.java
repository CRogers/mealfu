package uk.callumr.eventstore.core.internal;

import org.immutables.value.Value;

@Value.Immutable
public interface EventId {
    long eventId();

    static EventId of(long eventId) {
        return ImmutableEventId.builder()
                .eventId(eventId)
                .build();
    }
}
