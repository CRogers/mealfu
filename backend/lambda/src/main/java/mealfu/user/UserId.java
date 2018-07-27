package mealfu.user;

import mealfu.ids.MealfuEntityId;
import org.immutables.value.Value;

@Value.Immutable
public abstract class UserId extends MealfuEntityId<RecipeCreatedByUser> {

    @Override
    public String entityType() {
        return "user";
    }

    @Override
    public Class<RecipeCreatedByUser> eventClass() {
        return RecipeCreatedByUser.class;
    }

    static UserId of(String id) {
        return ImmutableUserId.builder()
                .identifier(id)
                .build();
    }

    static UserId random() {
        return random(UserId::of);
    }
}

