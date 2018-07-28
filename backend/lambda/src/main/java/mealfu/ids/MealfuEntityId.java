package mealfu.ids;

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

import java.util.List;
import java.util.function.Function;

public interface MealfuEntityId<TEvent extends MealfuEvent<?>> extends EntityId {
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

    static <TId extends MealfuEntityId> TId parse(Function<String, TId> ctor, String stringRep) {
        List<String> parts = Splitter.on('-')
                .limit(2)
                .splitToList(stringRep);

        Preconditions.checkArgument(parts.size() == 2, "Must be splittable on first -");

        TId id = ctor.apply(Iterables.get(parts, 1));

        String deserializedType = parts.get(0);
        Preconditions.checkArgument(deserializedType.equals(id.entityType()), "Deserialized as %s but were expecting a %s", deserializedType, id.entityType());

        return id;
    }

    static <T extends MealfuEntityId> T random(Function<String, T> ctor) {
        return ctor.apply(RandomStringUtils.randomHexString(16));
    }

    default Event just(MealfuEvent<? extends MealfuEntityId<TEvent>> mealfuEvent) {
        return ((MealfuEvent) mealfuEvent).withId(this);
    }
}
