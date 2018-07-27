package mealfu.ids;

import com.fasterxml.jackson.annotation.JsonValue;
import mealfu.RandomStringUtils;
import uk.callumr.eventstore.core.EntityId;

import java.util.function.Function;

public abstract class MealfuEntityId<TEvent> implements EntityId {
    public abstract String entityType();
    public abstract String identifier();
    public abstract Class<TEvent> eventClass();

    @JsonValue
    public final String asString() {
        return entityType() + "-" + identifier();
    }

    protected static <T extends MealfuEntityId> T random(Function<String, T> ctor) {
        return ctor.apply(RandomStringUtils.randomHexString(16));
    }
}
