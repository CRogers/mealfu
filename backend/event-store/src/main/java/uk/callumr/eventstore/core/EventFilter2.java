package uk.callumr.eventstore.core;

import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;

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

    public <T> T toCondition(
            BinaryOperator<T> and,
            BinaryOperator<T> or,
            Function<Set<EntityId>, T> entityIdsCondition,
            Function<Set<EventType>, T> eventTypesCondition) {

        return filters().stream()
                .map(eventFilter -> eventFilter.toCondition(and, entityIdsCondition, eventTypesCondition))
                .reduce(or)
                .orElseThrow(() -> new IllegalArgumentException("Must narrow search to at least one filter"));
    }
}
