package mealfu.ids;

import com.fasterxml.jackson.annotation.JsonValue;
import mealfu.RandomStringUtils;
import uk.callumr.eventstore.core.EntityId;

import java.util.function.Function;

public interface MealfuEntityId<TEvent> extends EntityId {
    String entityType();
    String identifier();
    Class<TEvent> eventClass();

    @JsonValue
    default String asString() {
        return entityType() + "-" + identifier();
    }

    static <T extends MealfuEntityId> T random(Function<String, T> ctor) {
        return ctor.apply(RandomStringUtils.randomHexString(16));
    }
}
