package mealfu.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import mealfu.ids.MealfuEntityId;
import org.immutables.value.Value;

@Value.Immutable
public abstract class RecipeId extends MealfuEntityId<RecipeCreatedByUser> {
    public abstract String identifier();

    @Override
    public Class<RecipeCreatedByUser> eventClass() {
        return RecipeCreatedByUser.class;
    }

    @Override
    public String entityType() {
        return "recipe";
    }

    @JsonCreator
    public static RecipeId of(String id) {
        return ImmutableRecipeId.builder()
                .identifier(id)
                .build();
    }

    public static RecipeId random() {
        return random(RecipeId::of);
    }
}

