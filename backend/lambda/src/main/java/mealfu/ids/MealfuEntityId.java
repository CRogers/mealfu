package mealfu.ids;

import mealfu.RandomStringUtils;
import uk.callumr.eventstore.core.EntityId;

import java.util.function.Function;

public abstract class MealfuEntityId implements EntityId {
    protected abstract String entityType();
    public abstract String identifier();

    public final String asString() {
        return entityType() + "-" + identifier();
    }

    protected static <T extends MealfuEntityId> T random(Function<String, T> ctor) {
        return ctor.apply(RandomStringUtils.randomHexString(16));
    }
}
