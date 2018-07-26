package uk.callumr.eventstore;

import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
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
    public void addEvent(Event event) {
        lock.write_(() -> addEventUnlocked(event));
    }

    @Override
    public Stream<VersionedEvent> events(EventFilters filters) {
        return lock.read(() -> eventsUnlocked(filters));
    }

    @Override
    public void withEvents(EventFilters filters, Function<Stream<VersionedEvent>, Stream<Event>> projectionFunc) {
        new CallExecutor<>(new RetryConfigBuilder()
                .withMaxNumberOfTries(10)
                .withNoWaitBackoff()
                .withDelayBetweenTries(Duration.ZERO)
                .retryOnSpecificExceptions(ConcurrentModificationException.class)
                .build())
                .execute(() -> {
                    Stream<VersionedEvent> events = events(filters);

                    AtomicReference<Optional<Long>> lastVersion = new AtomicReference<>(Optional.empty());

                    List<Event> newEvents = projectionFunc.apply(events
                            .peek(event -> lastVersion.set(Optional.of(event.version()))))
                            .collect(Collectors.toList());

                    lock.write_(() -> {
                        long mostRecentEventVersion = events(filters)
                                .map(VersionedEvent::version)
                                .reduce((a, b) -> b)
                                .orElse(Long.MIN_VALUE);

                        if (lastVersion.get().isPresent() && mostRecentEventVersion > lastVersion.get().get()) {
                            throw new ConcurrentModificationException();
                        }

                        newEvents.forEach(this::addEvent);
                    });

                    return null;
                });
    }

    private boolean addEventUnlocked(Event event) {
        return events.add(VersionedEvent.builder()
                .version(version.getAndIncrement())
                .event(BasicEvent.builder()
                        .entityId(event.entityId())
                        .eventType(event.eventType())
                        .data(event.data())
                        .build())
                .build()
        );
    }

    private Stream<VersionedEvent> eventsUnlocked(EventFilters filters) {
        Predicate<Event> eventPredicate = filters.stream().reduce(
                event -> false,
                (predicate, eventFilter) -> predicate.or(EventFilter.caseOf(eventFilter)
                        .forEntity(eventValueEqualTo(Event::entityId))
                        .ofType(eventValueEqualTo(Event::eventType))),
                Predicate::or);

        return events.stream()
                .filter(versionedEvent -> eventPredicate.test(versionedEvent.event()));
    }

    private static <T> Function<T, Predicate<Event>> eventValueEqualTo(Function<Event, T> extractor) {
        return value -> event -> value.equals(extractor.apply(event));
    }
}
