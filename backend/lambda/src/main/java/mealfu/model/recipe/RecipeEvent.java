package mealfu.model.recipe;

import mealfu.events.MealfuEvent;
import mealfu.model.user.UserId;
import org.derive4j.Data;
import uk.callumr.derive4jackson.JsonTypeNamePrefix;

@Data
@RecipeEvents.AllJsonSubTypes
public abstract class RecipeEvent implements MealfuEvent<RecipeId> {

    @JsonTypeNamePrefix("recipe-")
    interface Cases<R> {
        R Created(UserId creator);
        R NameChanged(RecipeName recipeName);
    }

    public abstract <R> R match(Cases<R> cases);

    @Override
    public abstract int hashCode();
    @Override
    public abstract boolean equals(Object obj);
    @Override
    public abstract String toString();
}
