package mealfu.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import mealfu.ids.MealfuEntityId;
import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.core.EventFilters;

import java.io.IOException;
import java.util.stream.Stream;

public class MealfuEventStore {
    private final EventStore eventStore;

    public MealfuEventStore(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public <TEvent> Stream<TEvent> events(MealfuEntityId<TEvent> id) {
        return eventStore.events(EventFilters.forEntity(id))
                .map(versionedEvent -> {
                    try {
                        return new ObjectMapper().readValue(versionedEvent.event().data(), id.eventClass());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
