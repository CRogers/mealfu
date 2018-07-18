package uk.callumr.eventstore.core;

import org.derive4j.Data;
import org.derive4j.Derive;
import org.derive4j.Visibility;

@Data(value = @Derive(inClass = "EventFilterImpls", withVisibility = Visibility.Package))
public abstract class EventFilter {
    public interface Cases<R> {
        R forEntity(EntityId entityId);
        R ofType(EventType eventType);
        R all();
    }

    public abstract <R> R match(Cases<R> cases);

    public static EventFilterImpls.CaseOfMatchers.TotalMatcher_ForEntity caseOf(EventFilter eventFilter) {
        return EventFilterImpls.caseOf(eventFilter);
    }

    @Override
    public abstract int hashCode();
    @Override
    public abstract boolean equals(Object obj);
    @Override
    public abstract String toString();
}
