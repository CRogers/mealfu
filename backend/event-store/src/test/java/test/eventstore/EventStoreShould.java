package test.eventstore;

import org.junit.Test;
import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.core.*;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.callumr.eventstore.core.EventFilter.forEntity;

public abstract class EventStoreShould {
    private static final BasicEntityId JAMES = BasicEntityId.of("james");
    private static final BasicEntityId ALEX = BasicEntityId.of("alex");
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
        eventStore.addEvents(event);

        Stream<Event> events = eventStore.eventsFor(JAMES).events();

        assertThat(events).containsExactly(event);
    }

    @Test
    public void return_only_new_events_when_giving_back_an_event_token() {
        Event event1 = Event.of(JAMES, EVENT_TYPE, EVENT_DATA);
        eventStore.addEvents(event1);

        Optional<EventToken> eventToken = eventStore.eventsFor(JAMES).eventToken();
        assertThat(eventToken).isNotEmpty();

        Event event2 = Event.of(JAMES, OTHER_EVENT_TYPE, EVENT_DATA);
        eventStore.addEvents(event2);

        Stream<Event> events = eventStore.events(forEntity(JAMES).since(eventToken)).events();
        assertThat(events).containsExactly(event2);
    }

    @Test
    public void return_two_events_in_insertion_order_when_inserted_for_the_same_entity() {
        Event jamesEvent1 = EVENT_TYPE.newEvent(JAMES, EVENT_DATA);
        Event jamesEvent2 = EVENT_TYPE.newEvent(JAMES, OTHER_EVENT_DATA);

        eventStore.addEvents(jamesEvent1);
        eventStore.addEvents(jamesEvent2);

        Stream<Event> events = eventStore.eventsFor(JAMES).events();

        assertThat(events).containsExactly(
                jamesEvent1,
                jamesEvent2);
    }

    @Test
    public void get_events_with_filter_for_just_entity_id() {
        Event jamesEvent = Event.of(JAMES, EVENT_TYPE, EVENT_DATA);
        Event alexEvent = Event.of(ALEX, EVENT_TYPE, EVENT_DATA);

        eventStore.addEvents(jamesEvent);
        eventStore.addEvents(alexEvent);

        Stream<Event> events = eventStore.eventsFor(JAMES).events();

        assertThat(events).containsExactly(jamesEvent);
    }

    @Test
    public void get_events_with_filter_for_just_entity_id_and_event_type() {
        Event someEvent = Event.of(JAMES, EVENT_TYPE, EVENT_DATA);
        Event otherEvent = Event.of(JAMES, OTHER_EVENT_TYPE, EVENT_DATA);

        eventStore.addEvents(someEvent);
        eventStore.addEvents(otherEvent);

        Stream<Event> events = eventStore.eventsFor(JAMES, EVENT_TYPE).events();

        assertThat(events).containsExactly(someEvent);
    }

    @Test
    public void reprojection_should_return_events_then_persist_new_events() {
        Event event1 = Event.of(JAMES, EVENT_TYPE, EVENT_DATA);
        Event event2 = Event.of(JAMES, EVENT_TYPE, OTHER_EVENT_DATA);
        Event event3 = Event.of(ALEX, EVENT_TYPE, EVENT_DATA);

        eventStore.addEvents(event1);
        eventStore.addEvents(event2);
        eventStore.addEvents(event3);

        Event event4 = Event.of(ALEX, OTHER_EVENT_TYPE, OTHER_EVENT_DATA);

        eventStore.withEvents(forEntity(JAMES), events -> {
            assertThat(events).containsExactly(
                    event1,
                    event2);

            return Stream.of(event4);
        });

        assertThat(eventStore.eventsFor(ALEX, OTHER_EVENT_TYPE).events()).containsExactly(event4);
    }

    @Test
    public void reprojection_should_take_into_account_new_events_if_added_when_the_projection_is_being_calculated() {
        Event event1 = Event.of(JAMES, EVENT_TYPE, EVENT_DATA);
        Event event2 = Event.of(JAMES, OTHER_EVENT_TYPE, EVENT_DATA);

        eventStore.addEvents(event1);

        AtomicBoolean runOnce = new AtomicBoolean(false);

        eventStore.withEvents(forEntity(JAMES), events -> {
            long count = events.count();
            if (!runOnce.get()) {
                eventStore.addEvents(event2);
                runOnce.set(true);
            }
            return Stream.of(Event.of(ALEX, EVENT_TYPE, Long.toString(count)));
        });

        assertThat(eventStore.eventsFor(ALEX).events()).containsExactly(
                Event.of(ALEX, EVENT_TYPE, "2"));
    }

    @Test
    public void reprojection_should_work_when_the_user_does_not_consume_all_events() {
        Event event1 = Event.of(JAMES, EVENT_TYPE, EVENT_DATA);
        Event event2 = Event.of(JAMES, OTHER_EVENT_TYPE, EVENT_DATA);

        eventStore.addEvents(event1, event2, event2, event2, event2, event2);

        eventStore.withEvents(forEntity(JAMES), events -> {
            return Stream.of(event1);
        });
    }

    @Test
    public void reprojection_should_allow_returning_no_events() {
        eventStore.withEvents(forEntity(JAMES), events -> {
            assertThat(events).isEmpty();
            return Stream.of();
        });
    }

}
