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
    public Events events(EventFilter eventFilter) {
        Stream<Event> eventStream = eventsUnlocked(filterToPredicate(eventFilter))
                .map(VersionedEvent::event);

        return Events.builder()
                .consecutiveEventStreams(eventStream)
                .eventToken(EventToken.unimplemented())
                .build();
    }

    private Stream<VersionedEvent> events(Predicate<Event> predicate) {
        return lock.read(() -> eventsUnlocked(predicate));
    }

    @Override
    public void withEvents(EventFilter eventFilter, Function<EntryStream<EntityId, Stream<Event>>, Stream<Event>> projectionFunc) {
        Predicate<Event> predicate = eventFilterToPredicate(eventFilter);

        withEventsInner(predicate, projectionFunc);
    }

    private void withEventsInner(Predicate<Event> predicate, Function<EntryStream<EntityId, Stream<Event>>, Stream<Event>> projectionFunc) {
        new CallExecutor<>(new RetryConfigBuilder()
                .withMaxNumberOfTries(10)
                .withNoWaitBackoff()
                .withDelayBetweenTries(Duration.ZERO)
                .retryOnSpecificExceptions(ConcurrentModificationException.class)
                .build())
                .execute(() -> {
                    Stream<VersionedEvent> events = events(predicate);

                    AtomicReference<Optional<Long>> lastVersion = new AtomicReference<>(Optional.empty());

                    EntryStream<EntityId, Stream<Event>> eventsToFeedToProjection = Events.consecutiveEventsToEntryStream(events
                            .peek(event -> lastVersion.set(Optional.of(event.version())))
                            .map(VersionedEvent::event));

                    List<Event> newEvents = projectionFunc.apply(eventsToFeedToProjection)
                            .collect(Collectors.toList());

                    lock.write_(() -> {
                        long mostRecentEventVersion = events(predicate)
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

    private void addEventUnlocked(Event event) {
        events.add(VersionedEvent.builder()
                .version(version.getAndIncrement())
                .event(Event.builder()
                        .entityId(event.entityId())
                        .eventType(event.eventType())
                        .data(event.data())
                        .build())
                .build()
        );
    }

    private Predicate<Event> filterToPredicate(EventFilter eventFilter) {
        return eventFilter.toCondition(
                Predicate::and,
                entityIds -> event -> entityIds.contains(event.entityId()),
                eventTypes -> event -> eventTypes.contains(event.eventType()));
    }

    private Stream<VersionedEvent> eventsUnlocked(Predicate<Event> eventPredicate) {
        return events.stream()
                .filter(versionedEvent -> eventPredicate.test(versionedEvent.event()));
    }

    private Predicate<Event> eventFilterToPredicate(EventFilter eventFilters) {
        return eventFilters.toCondition(
                Predicate::and,
                entityIds -> event -> entityIds.contains(event.entityId()),
                eventTypes -> event -> eventTypes.contains(event.eventType()));
    }

}
