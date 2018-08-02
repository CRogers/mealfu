package mealfu.events;

import mealfu.ids.MealfuEntityId;
import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.core.Event;

import java.util.stream.Stream;

public class MealfuEventStore {
    private final EventStore eventStore;

    public MealfuEventStore(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public <TEvent extends MealfuEvent<?>> Stream<TEvent> events(MealfuEntityId<TEvent> id) {
        return eventStore.eventsFor(id).events()
                .map(event -> MealfuEvent.fromJson(event.data(), id.eventClassFor(event.eventType())));
    }

    public void addEvents(Event... events) {
        eventStore.addEvents(events);
    }
}
