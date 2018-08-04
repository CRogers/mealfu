package mealfu.events;

import mealfu.ids.MealfuEntityId;
import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.core.Event;
import uk.callumr.eventstore.core.EventType;

import java.util.Arrays;
import java.util.stream.Stream;

public class MealfuEventStore {
    private final EventStore eventStore;

    public MealfuEventStore(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @SuppressWarnings("unchecked")
    public <TEvent extends MealfuEvent<?>> Stream<TEvent> eventsFor(MealfuEntityId<TEvent> id, Class<? extends TEvent>... eventTypes) {
        EventType[] eventTypesToFetch = Arrays.stream(eventTypes)
                .map(MealfuEvent::jsonTypeNameFor)
                .map(EventType::of)
                .toArray(EventType[]::new);

        return eventStore.eventsFor(id, eventTypesToFetch).events()
                .map(event -> MealfuEvent.fromJson(event.data(), id.eventClassFor(event.eventType())));
    }

    public <TEvent extends MealfuEvent<?>, TEventConstructor extends TEvent> Stream<TEventConstructor> eventsFor(
            MealfuEntityId<TEvent> id, Class<TEventConstructor> eventType) {
        return eventsFor(id, new Class[] { eventType });
    }

    public void addEvents(Event... events) {
        eventStore.addEvents(events);
    }
}
