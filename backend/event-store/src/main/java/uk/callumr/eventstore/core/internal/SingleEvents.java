package uk.callumr.eventstore.core.internal;

import org.immutables.value.Value;
import uk.callumr.eventstore.core.Event;
import uk.callumr.eventstore.core.EventToken;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Value.Immutable
public abstract class SingleEvents {
    protected abstract Supplier<Optional<EventToken>> eventTokenSupplier();
    public abstract Stream<Event> events();

    public Optional<EventToken> eventToken() {
        return eventTokenSupplier().get();
    }

    public static class Builder extends ImmutableSingleEvents.Builder { }

    public static Builder builder() {
        return new Builder();
    }
}
