package uk.callumr.eventstore.core;

import one.util.streamex.EntryStream;
import org.immutables.value.Value;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value.Immutable
public abstract class Events {
    public abstract EventToken eventToken();
    protected abstract Stream<Event> consecutiveEventStreams();

    public EntryStream<EntityId, Stream<Event>> eventStreams() {
        return EntryStream.of(consecutiveEventStreams()
                .map(event -> ImmutableEntityIdAnd.of(event.entityId(), event)))
                .collapseKeys(Collectors.toList()) // This can be made way more lazy by not using collapseKeys
                .mapValues(Collection::stream);
    }

    public static class Builder extends ImmutableEvents.Builder { }

    public static Builder builder() {
        return new Builder();
    }

    @Value.Immutable
    interface EntityIdAnd<V> extends Map.Entry<EntityId, V> {
        @Value.Parameter EntityId getKey();
        @Value.Parameter V getValue();

        @Override
        default V setValue(V value) {
            throw new UnsupportedOperationException();
        }
    }
}
