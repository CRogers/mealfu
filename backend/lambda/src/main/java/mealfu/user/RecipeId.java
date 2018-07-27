package mealfu.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import mealfu.ids.MealfuEntityId;
import org.immutables.value.Value;

@Value.Immutable
public interface RecipeId extends MealfuEntityId<RecipeCreatedByUser> {
    String identifier();

    @Override
    default Class<RecipeCreatedByUser> eventClass() {
        return RecipeCreatedByUser.class;
    }

    @Override
    default String entityType() {
        return "recipe";
    }

    @JsonCreator
    static RecipeId of(String id) {
        return ImmutableRecipeId.builder()
                .identifier(id)
                .build();
    }

    static RecipeId random() {
        return MealfuEntityId.random(RecipeId::of);
    }
}

