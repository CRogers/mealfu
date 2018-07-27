package mealfu.ids;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import mealfu.RandomStringUtils;
import mealfu.events.MealfuEvent;
import org.immutables.value.Value;
import uk.callumr.eventstore.core.BasicEventType;
import uk.callumr.eventstore.core.EntityId;
import uk.callumr.eventstore.core.Event;

import java.util.function.Function;

public interface MealfuEntityId<TEvent> extends EntityId {
    String entityType();
    String identifier();
    Class<? extends TEvent> eventClassFor(BasicEventType eventType);

    @Value.Check
    default void verifyFormattedCorrectly() {
        Preconditions.checkArgument(entityType().matches("\\w+"), "entityType must be a single alphanumeric word");
        Preconditions.checkArgument(identifier().matches("[\\w-]+"), "identifier must be a series of alphanumeric words seperated by dashes");
    }

    @JsonValue
    default String asString() {
        return entityType() + "-" + identifier();
    }

    @JsonCreator
    static <T extends MealfuEntityId> T parse(Function<String, T> ctor, String stringRep) {
        Iterable<String> parts = Splitter.on('-')
                .limit(1)
                .split(stringRep);

        return ctor.apply(Iterables.get(parts, 1));
    }

    static <T extends MealfuEntityId> T random(Function<String, T> ctor) {
        return ctor.apply(RandomStringUtils.randomHexString(16));
    }

    default Event just(MealfuEvent<MealfuEntityId<TEvent>> mealfuEvent) {
        return mealfuEvent.withId(this);
    }
}
