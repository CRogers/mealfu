package mealfu.events;

import mealfu.ids.MealfuEntityId;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.core.Event;
import uk.callumr.eventstore.core.EventFilter;
import uk.callumr.eventstore.core.EventType;
import uk.callumr.eventstore.core.Events;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MealfuEventStore {
    private final EventStore eventStore;

    public MealfuEventStore(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @SuppressWarnings("unchecked")
    public <TEvent extends MealfuEvent<?>> Stream<TEvent> eventsFor(MealfuEntityId<TEvent> id, Class<? extends TEvent>... eventTypes) {
        EventType[] eventTypesToFetch = fromEventTypeClasses(eventTypes)
                .toArray(EventType[]::new);

        return eventStore.eventsFor(id, eventTypesToFetch).events()
                .map(toTypedEventFor(id));
    }

    public <TEvent extends MealfuEvent<?>, TEventConstructor extends TEvent> Stream<TEventConstructor> eventsFor(
            MealfuEntityId<TEvent> id, Class<TEventConstructor> eventType) {
        return eventsFor(id, new Class[] { eventType });
    }

    public <TEntityId extends MealfuEntityId<TEvent>, TEvent extends MealfuEvent<?>> EntryStream<TEntityId, StreamEx<TEvent>> eventsFor(
            Iterable<TEntityId> entityIds,
            Class<? extends TEvent>... eventTypes) {

        Events events = eventStore.events(EventFilter.builder()
                .entityIds(entityIds)
                .eventTypes(fromEventTypeClasses(eventTypes).collect(Collectors.toList()))
                .build());

        return events.eventStreams()
                .mapKeys(entityId -> (TEntityId) entityId)
                .mapValues(StreamEx::of)
                .mapToValue((entityId, eventStream) -> eventStream.map(toTypedEventFor(entityId)));
    }

    public void addEvents(Event... events) {
        eventStore.addEvents(events);
    }

    private <TEvent extends MealfuEvent<?>> Stream<EventType> fromEventTypeClasses(Class<? extends TEvent>[] eventTypes) {
        return Arrays.stream(eventTypes)
                .map(MealfuEvent::jsonTypeNameFor)
                .map(EventType::of);
    }

    private <TEvent extends MealfuEvent<?>> Function<Event, TEvent> toTypedEventFor(MealfuEntityId<TEvent> id) {
        return event -> MealfuEvent.fromJson(event.data(), id.eventClassFor(event.eventType()));
    }
}
