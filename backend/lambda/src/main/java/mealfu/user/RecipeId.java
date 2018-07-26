package mealfu.user;

import mealfu.ids.MealfuEntityId;
import org.immutables.value.Value;

@Value.Immutable
public abstract class RecipeId extends MealfuEntityId {
    @Override
    protected String entityType() {
        return "recipe";
    }
    
    static RecipeId of(String id) {
        return ImmutableRecipeId.builder()
                .identifier(id)
                .build();
    }

    static RecipeId random() {
        return random(RecipeId::of);
    }
}

