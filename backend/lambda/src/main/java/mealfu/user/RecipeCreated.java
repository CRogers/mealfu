package mealfu.user;
import org.immutables.value.Value;

@Value.Immutable
public interface RecipeCreated {
    RecipeId recipeId();

    class Builder extends ImmutableRecipeCreated.Builder { }

    static Builder builder() {
        return new Builder();
    }
}

