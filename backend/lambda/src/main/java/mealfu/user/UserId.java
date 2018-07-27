package mealfu.user;

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

    static UserId random() {
        return MealfuEntityId.random(UserId::of);
    }
}

