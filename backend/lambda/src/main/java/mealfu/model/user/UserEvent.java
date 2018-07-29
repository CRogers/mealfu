package mealfu.model.user;

import mealfu.events.MealfuEvent;
import mealfu.model.recipe.RecipeId;
import org.derive4j.Data;
import uk.callumr.derive4jackson.JsonTypeNamePrefix;

@Data
@UserEvents.AllJsonSubTypes
public abstract class UserEvent implements MealfuEvent<UserId> {
    @JsonTypeNamePrefix("user-")
    interface Cases<R> {
        R AddedRecipe(RecipeId recipeId);
    }

    public abstract <R> R match(Cases<R> cases);

    @Override
    public abstract int hashCode();
    @Override
    public abstract boolean equals(Object obj);
    @Override
    public abstract String toString();
}
