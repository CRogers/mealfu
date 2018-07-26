package mealfu.user;
import org.immutables.value.Value;

@Value.Immutable
public interface RecipeCreatedByUser {
    RecipeId recipeId();

    class Builder extends ImmutableRecipeCreatedByUser.Builder { }

    static Builder builder() {
        return new Builder();
    }
}

