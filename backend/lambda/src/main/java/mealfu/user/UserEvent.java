package mealfu.user;

import mealfu.events.MealfuEvent;
import org.derive4j.Data;
import uk.callumr.eventstore.core.BasicEventType;

@Data
public abstract class UserEvent implements MealfuEvent<UserId> {
    private static final String CREATED_RECIPE = "created-recipe";

    interface Cases<R> {
        R CreatedRecipe(RecipeId recipeId);
    }

    public abstract <R> R match(Cases<R> cases);

    public static Class<? extends UserEvent> classFor(BasicEventType eventType) {
        switch (eventType.asString()) {
            case CREATED_RECIPE:
                return UserEvents.CreatedRecipe.class;
        }

        throw new RuntimeException("Should never happen");
    }

    public BasicEventType eventType() {
        return BasicEventType.of(UserEvents.cases()
                .CreatedRecipe_("created-recipe")
                .apply(this));
    }

    @Override
    public abstract int hashCode();
    @Override
    public abstract boolean equals(Object obj);
    @Override
    public abstract String toString();
}
