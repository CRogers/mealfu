package uk.callumr.eventstore.core;

import org.immutables.value.Value;
import uk.callumr.eventstore.core.internal.EventId;

@Value.Immutable
public abstract class EventToken {
    public abstract EventId lastEventAccessed();

    public static EventToken of(EventId lastEventAccessed) {
        return ImmutableEventToken.builder()
                .lastEventAccessed(lastEventAccessed)
                .build();
    }
}
