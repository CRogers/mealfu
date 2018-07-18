package test.eventstore;

import org.junit.Test;
import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.core.EntityId;
import uk.callumr.eventstore.core.Event;
import uk.callumr.eventstore.core.EventType;
import uk.callumr.eventstore.core.VersionedEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.callumr.eventstore.core.EventFilters.*;

public abstract class EventStoreShould {
    private static final EntityId JAMES = EntityId.of("james");
    private static final EntityId ALEX = EntityId.of("alex");
    private static final EventType EVENT_TYPE = EventType.of("eventType");
    private static final EventType OTHER_EVENT_TYPE = EventType.of("otherEventType");
    private static final String EVENT_DATA = "eventData";
    private static final String OTHER_EVENT_DATA = "other eventData";

    private final EventStore eventStore;

    public EventStoreShould(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Test
    public void return_an_event_given_one_was_inserted_for_a_given_entity_id() {
        Event event = EVENT_TYPE.newEvent(JAMES, EVENT_DATA);
        eventStore.addEvent(event);

        Stream<VersionedEvent> events = eventStore.events(forEntity(JAMES));

        assertThatSteamContainsEvents(events, event);
    }

    @Test
    public void return_two_events_in_insertion_order_when_inserted_for_the_same_entity() {
        Event jamesEvent1 = EVENT_TYPE.newEvent(JAMES, EVENT_DATA);
        Event jamesEvent2 = EVENT_TYPE.newEvent(JAMES, OTHER_EVENT_DATA);

        eventStore.addEvent(jamesEvent1);
        eventStore.addEvent(jamesEvent2);

        Stream<VersionedEvent> events = eventStore.events(forEntity(JAMES));

        assertThatSteamContainsEvents(events,
                jamesEvent1,
                jamesEvent2);
    }

    @Test
    public void get_events_with_filter_for_just_entity_id() {
        Event jamesEvent = Event.of(JAMES, EVENT_TYPE, EVENT_DATA);
        Event alexEvent = Event.of(ALEX, EVENT_TYPE, EVENT_DATA);

        eventStore.addEvent(jamesEvent);
        eventStore.addEvent(alexEvent);

        Stream<VersionedEvent> events = eventStore.events(forEntity(JAMES));

        assertThatSteamContainsEvents(events, jamesEvent);
    }

    @Test
    public void get_events_with_filter_for_just_event_type() {
        Event someEvent = Event.of(JAMES, EVENT_TYPE, EVENT_DATA);
        Event otherEvent = Event.of(JAMES, OTHER_EVENT_TYPE, EVENT_DATA);

        eventStore.addEvent(someEvent);
        eventStore.addEvent(otherEvent);

        Stream<VersionedEvent> events = eventStore.events(ofType(EVENT_TYPE));

        assertThatSteamContainsEvents(events, someEvent);
    }

    @Test
    public void get_events_with_filter_for_all_should_return_all_events() {
        Event event1 = Event.of(JAMES, EVENT_TYPE, EVENT_DATA);
        Event event2 = Event.of(JAMES, OTHER_EVENT_TYPE, EVENT_DATA);
        Event event3 = Event.of(ALEX, EVENT_TYPE, EVENT_DATA);

        eventStore.addEvent(event1);
        eventStore.addEvent(event2);
        eventStore.addEvent(event3);

        Stream<VersionedEvent> events = eventStore.events(all());

        assertThatSteamContainsEvents(events, event1, event2, event3);
    }

    @Test
    public void reprojection_should_return_events_then_persist_new_events() {
        Event event1 = Event.of(JAMES, EVENT_TYPE, EVENT_DATA);
        Event event2 = Event.of(JAMES, EVENT_TYPE, OTHER_EVENT_DATA);
        Event event3 = Event.of(ALEX, EVENT_TYPE, EVENT_DATA);

        eventStore.addEvent(event1);
        eventStore.addEvent(event2);
        eventStore.addEvent(event3);

        Event event4 = Event.of(ALEX, OTHER_EVENT_TYPE, OTHER_EVENT_DATA);

        eventStore.withEvents(forEntity(JAMES), events -> {
            assertThatSteamContainsEvents(events,
                    event1,
                    event2);

            return Stream.of(event4);
        });

        assertThatSteamContainsEvents(eventStore.events(ofType(OTHER_EVENT_TYPE)), event4);
    }

    @Test
    public void reprojection_should_take_into_account_new_events_if_added_when_the_projection_is_being_calculated() {
        Event event1 = Event.of(JAMES, EVENT_TYPE, EVENT_DATA);
        Event event2 = Event.of(JAMES, OTHER_EVENT_TYPE, EVENT_DATA);

        eventStore.addEvent(event1);

        AtomicBoolean runOnce = new AtomicBoolean(false);

        eventStore.withEvents(forEntity(JAMES), events -> {
            long count = events.count();
            if (!runOnce.get()) {
                eventStore.addEvent(event2);
                runOnce.set(true);
            }
            return Stream.of(Event.of(ALEX, EVENT_TYPE, Long.toString(count)));
        });

        assertThatSteamContainsEvents(eventStore.events(forEntity(ALEX)),
                Event.of(ALEX, EVENT_TYPE, "2"));
    }

    private static void assertThatSteamContainsEvents(Stream<VersionedEvent> eventStream, Event... expectedEvents) {
        assertThat(eventStream.map(VersionedEvent::event)).containsExactly(expectedEvents);
    }

}
