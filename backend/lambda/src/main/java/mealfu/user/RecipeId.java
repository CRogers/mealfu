package mealfu.user;

import org.immutables.value.Value;

@Value.Immutable
public interface RecipeId {
    String asString();

    class Builder extends ImmutableRecipeId.Builder { }

    static Builder builder() {
        return new Builder();
    }
    
    static RecipeId of(String recipeId) {
        return builder().asString(recipeId).build();
    }
}

