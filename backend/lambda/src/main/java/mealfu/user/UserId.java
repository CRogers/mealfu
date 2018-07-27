package mealfu.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import mealfu.ids.MealfuEntityId;
import org.immutables.value.Value;
import uk.callumr.eventstore.core.BasicEventType;

@Value.Immutable
public interface UserId extends MealfuEntityId<UserEvent> {

    @Override
    default String entityType() {
        return "user";
    }

    @Override
    default Class<? extends UserEvent> eventClassFor(BasicEventType eventType) {
        return UserEvent.classFor(eventType);
    }

    static UserId of(String id) {
        return ImmutableUserId.builder()
                .identifier(id)
                .build();
    }

    @JsonCreator
    static UserId parse(String stringId) {
        return MealfuEntityId.parse(UserId::of, stringId);
    }

    static UserId random() {
        return MealfuEntityId.random(UserId::of);
    }
}

