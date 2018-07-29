package mealfu.model.user;

import mealfu.events.MealfuEvent;
import mealfu.model.recipe.RecipeId;
import org.derive4j.Data;
import uk.callumr.derive4jackson.JsonTypeNamePrefix;
import uk.callumr.eventstore.core.BasicEventType;

@Data
@UserEvents.AllJsonSubTypes
public abstract class UserEvent implements MealfuEvent<UserId> {
    private static final String ADDED_RECIPE = "added-recipe";

    @JsonTypeNamePrefix("user-")
    interface Cases<R> {
        R AddedRecipe(RecipeId recipeId);
    }

    public abstract <R> R match(Cases<R> cases);

    public static Class<? extends UserEvent> classFor(BasicEventType eventType) {
        switch (eventType.asString()) {
            case ADDED_RECIPE:
                return UserEvents.AddedRecipe.class;
        }

        throw new RuntimeException("Should never happen");
    }

    public BasicEventType eventType() {
        return BasicEventType.of(UserEvents.cases()
                .AddedRecipe_(ADDED_RECIPE)
                .apply(this));
    }

    @Override
    public abstract int hashCode();
    @Override
    public abstract boolean equals(Object obj);
    @Override
    public abstract String toString();
}
