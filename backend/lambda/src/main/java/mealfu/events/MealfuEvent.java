package mealfu.events;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import mealfu.ids.MealfuEntityId;
import uk.callumr.eventstore.core.BasicEventType;
import uk.callumr.eventstore.core.Event;

import java.io.IOException;

public interface MealfuEvent<Id extends MealfuEntityId> {
    ObjectMapper EVENT_OBJECT_MAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .registerModule(new Jdk8Module())
            .registerModule(new ParameterNamesModule());

    @JsonIgnore
    BasicEventType eventType();

    default String toJson() {
        try {
            return EVENT_OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    default Event withId(Id id) {
        return Event.of(id, eventType(), toJson());
    }

    static <T extends MealfuEvent<?>> T fromJson(String json, Class<T> clazz) {
        try {
            return EVENT_OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
