package mealfu.read.recipe;

import mealfu.model.recipe.RecipeId;
import mealfu.model.recipe.RecipeName;
import mealfu.model.user.UserId;
import org.immutables.value.Value;

@Value.Immutable
public interface Recipe {
    RecipeId recipeId();
    RecipeName recipeName();
    UserId createdBy();

    class Builder extends ImmutableRecipe.Builder { }

    static Builder builder() {
        return new Builder();
    }
}

