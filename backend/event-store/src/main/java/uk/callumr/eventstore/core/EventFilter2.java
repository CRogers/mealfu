package uk.callumr.eventstore.core;

import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class EventFilter2 {
    public abstract List<EventFilter3> filters();

    @Value.Check
    protected void check() {
        Preconditions.checkArgument(!filters().isEmpty(), "Must have at least one filter");
    }

    public static class Builder extends ImmutableEventFilter2.Builder { }

    public static Builder builder() {
        return new Builder();
    }
}
