package mealfu.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import mealfu.ids.MealfuEntityId;
import uk.callumr.eventstore.core.BasicEventType;
import uk.callumr.eventstore.core.Event;

public interface MealfuEvent<Id extends MealfuEntityId> {
    ObjectMapper EVENT_OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new Jdk8Module());

    @JsonIgnore
    BasicEventType eventType();

    default String toJson() {
        try {
            return EVENT_OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    default Event forEntity(Id id) {
        return Event.of(id, eventType(), toJson());
    }
}
