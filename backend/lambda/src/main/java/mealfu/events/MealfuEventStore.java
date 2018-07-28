package mealfu.events;

import mealfu.ids.MealfuEntityId;
import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.core.Event;
import uk.callumr.eventstore.core.EventFilters;

import java.util.stream.Stream;

public class MealfuEventStore {
    private final EventStore eventStore;

    public MealfuEventStore(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public <TEvent extends MealfuEvent<?>> Stream<TEvent> events(MealfuEntityId<TEvent> id) {
        return eventStore.events(EventFilters.forEntity(id))
                .map(versionedEvent -> {
                    Event event = versionedEvent.event();
                    return MealfuEvent.fromJson(event.data(), id.eventClassFor(event.eventType()));
                });
    }

    public void addEvents(Event... events) {
        eventStore.addEvents(events);
    }
}
