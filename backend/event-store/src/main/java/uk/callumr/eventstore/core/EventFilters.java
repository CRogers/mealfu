package uk.callumr.eventstore.core;

import org.immutables.value.Value;

import java.util.Set;
import java.util.stream.Stream;

@Value.Immutable
public abstract class EventFilters {
    protected abstract Set<EventFilter> filters();

    public Stream<EventFilter> stream() {
        return filters().stream();
    }

    public static class Builder extends ImmutableEventFilters.Builder {
        public Builder forEntity(EntityId entityId) {
            addFilters(EventFilterImpls.forEntity(entityId));
            return this;
        }

        public Builder ofType(EventType eventType) {
            addFilters(EventFilterImpls.ofType(eventType));
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static EventFilters forEntity(EntityId entityId) {
        return builder().forEntity(entityId).build();
    }

    public static EventFilters ofType(EventType eventType) {
        return builder().ofType(eventType).build();
    }
}
