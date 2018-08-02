package uk.callumr.eventstore;

import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import one.util.streamex.EntryStream;
import uk.callumr.eventstore.core.*;
import uk.callumr.eventstore.inmemory.EasyReadWriteLock;

import java.time.Duration;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryEventStore implements EventStore {
    private final AtomicLong version = new AtomicLong(0);
    private final List<VersionedEvent> events = new ArrayList<>();
    private final EasyReadWriteLock lock = new EasyReadWriteLock();

    @Override
    public void addEvents(Stream<Event> events) {
        lock.write_(() -> events.forEach(this::addEventUnlocked));
    }

    @Override
    public Events events(EventFilter2 eventFilters) {
        Stream<Event> eventStream = eventsUnlocked2(eventFilters)
                .map(VersionedEvent::event);
        return Events.builder()
                .consecutiveEventStreams(eventStream)
                .eventToken(EventToken.unimplemented())
                .build();
    }

    private Stream<VersionedEvent> oldEvents(Predicate<Event> predicate) {
        return lock.read(() -> eventsUnlocked(predicate));
    }

    @Override
    public void withEvents(EventFilters filters, Function<Stream<VersionedEvent>, Stream<Event>> projectionFunc) {
        Predicate<Event> predicate = oldEventFiltersToPredicate(filters);

        withEventsInner(predicate, projectionFunc);
    }

    @Override
    public void withEvents(EventFilter2 eventFilters, Function<EntryStream<EntityId, Event>, Stream<Event>> projectionFunc) {
        Predicate<Event> predicate = eventFiltersToPredicate(eventFilters);

        throw new UnsupportedOperationException();
    }

    private void withEventsInner(Predicate<Event> predicate, Function<Stream<VersionedEvent>, Stream<Event>> projectionFunc) {
        new CallExecutor<>(new RetryConfigBuilder()
                .withMaxNumberOfTries(10)
                .withNoWaitBackoff()
                .withDelayBetweenTries(Duration.ZERO)
                .retryOnSpecificExceptions(ConcurrentModificationException.class)
                .build())
                .execute(() -> {
                    Stream<VersionedEvent> events = oldEvents(predicate);

                    AtomicReference<Optional<Long>> lastVersion = new AtomicReference<>(Optional.empty());

                    List<Event> newEvents = projectionFunc.apply(events
                            .peek(event -> lastVersion.set(Optional.of(event.version()))))
                            .collect(Collectors.toList());

                    lock.write_(() -> {
                        long mostRecentEventVersion = oldEvents(predicate)
                                .map(VersionedEvent::version)
                                .reduce((a, b) -> b)
                                .orElse(Long.MIN_VALUE);

                        if (lastVersion.get().isPresent() && mostRecentEventVersion > lastVersion.get().get()) {
                            throw new ConcurrentModificationException();
                        }

                        newEvents.forEach(this::addEvents);
                    });

                    return null;
                });
    }

    private boolean addEventUnlocked(Event event) {
        return events.add(VersionedEvent.builder()
                .version(version.getAndIncrement())
                .event(Event.builder()
                        .entityId(event.entityId())
                        .eventType(event.eventType())
                        .data(event.data())
                        .build())
                .build()
        );
    }

    private Stream<VersionedEvent> eventsUnlocked2(EventFilter2 eventFilters) {
        Predicate<Event> eventPredicate = eventFilters.toCondition(
                Predicate::and,
                Predicate::or,
                entityIds -> event -> entityIds.contains(event.entityId()),
                eventTypes -> event -> eventTypes.contains(event.eventType()));

        return events.stream()
                .filter(versionedEvent -> eventPredicate.test(versionedEvent.event()));
    }

    private Stream<VersionedEvent> eventsUnlocked(Predicate<Event> eventPredicate) {
        return events.stream()
                .filter(versionedEvent -> eventPredicate.test(versionedEvent.event()));
    }

    private Predicate<Event> eventFiltersToPredicate(EventFilter2 eventFilters) {
        return eventFilters.toCondition(
                Predicate::and,
                Predicate::or,
                entityIds -> event -> entityIds.contains(event.entityId()),
                eventTypes -> event -> eventTypes.contains(event.eventType()));
    }

    private Predicate<Event> oldEventFiltersToPredicate(EventFilters filters) {
        return filters.stream().reduce(
                event -> false,
                (predicate, eventFilter) -> predicate.or(EventFilter.caseOf(eventFilter)
                        .forEntity(eventValueEqualTo(Event::entityId))
                        .ofType(eventValueEqualTo(Event::eventType))),
                Predicate::or);
    }

    private static <T> Function<T, Predicate<Event>> eventValueEqualTo(Function<Event, T> extractor) {
        return value -> event -> value.equals(extractor.apply(event));
    }
}
