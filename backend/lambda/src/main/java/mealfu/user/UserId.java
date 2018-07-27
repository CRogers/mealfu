package mealfu.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import mealfu.ids.MealfuEntityId;
import org.immutables.value.Value;

@Value.Immutable
public interface UserId extends MealfuEntityId<RecipeCreatedByUser> {

    @Override
    default String entityType() {
        return "user";
    }

    @Override
    default Class<RecipeCreatedByUser> eventClass() {
        return RecipeCreatedByUser.class;
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

