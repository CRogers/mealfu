package test.eventstore.impls;

import test.eventstore.EventStoreShould;
import uk.callumr.eventstore.InMemoryEventStore;

public class InMemoryEventStoreShould extends EventStoreShould {
    public InMemoryEventStoreShould() {
        super(new InMemoryEventStore());
    }
}
