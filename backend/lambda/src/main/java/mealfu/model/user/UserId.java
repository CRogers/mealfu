package mealfu.model.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import mealfu.ids.MealfuEntityId;
import org.immutables.value.Value;
import uk.callumr.eventstore.core.EventType;

@Value.Immutable
public abstract class UserId extends MealfuEntityId<UserEvent> {

    @Override
    public String entityType() {
        return "user";
    }

    @Override
    public Class<? extends UserEvent> eventClassFor(EventType eventType) {
        return UserEvents.typeNameToClass(eventType.asString());
    }

    public static UserId of(String id) {
        return ImmutableUserId.builder()
                .identifier(id)
                .build();
    }

    @JsonCreator
    public static UserId parse(String stringId) {
        return MealfuEntityId.parse(UserId::of, stringId);
    }

    public static UserId random() {
        return MealfuEntityId.random(UserId::of);
    }
}

