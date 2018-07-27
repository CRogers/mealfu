package mealfu.user;

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
    
    public static RecipeId of(String id) {
        return ImmutableRecipeId.builder()
                .identifier(id)
                .build();
    }

    public static RecipeId random() {
        return random(RecipeId::of);
    }
}

