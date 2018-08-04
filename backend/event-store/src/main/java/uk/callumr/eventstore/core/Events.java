package uk.callumr.eventstore.core;

import one.util.streamex.EntryStream;
import org.immutables.value.Value;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value.Immutable
public abstract class Events {
    protected abstract Supplier<Optional<EventToken>> eventTokenSupplier();
    protected abstract Stream<Event> consecutiveEventStreams();

    @Value.Derived
    public EntryStream<EntityId, Stream<Event>> eventStreams() {
        return consecutiveEventsToEntryStream(consecutiveEventStreams());
    }

    public Optional<EventToken> eventToken() {
        return eventTokenSupplier().get();
    }

    public static EntryStream<EntityId, Stream<Event>> consecutiveEventsToEntryStream(Stream<Event> events) {
        return EntryStream.of(events
                .map(event -> EntityIdAnd.of(event.entityId(), event)))
                .collapseKeys(Collectors.toList()) // This can be made way more lazy by not using collapseKeys
                .mapValues(Collection::stream);
    }

    public static class Builder extends ImmutableEvents.Builder { }

    public static Builder builder() {
        return new Builder();
    }

}
