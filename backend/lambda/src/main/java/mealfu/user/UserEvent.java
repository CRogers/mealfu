package mealfu.user;

import mealfu.events.MealfuEvent;
import org.derive4j.Data;
import uk.callumr.eventstore.core.BasicEventType;

@Data
public interface UserEvent extends MealfuEvent<UserId> {
    String CREATED_RECIPE = "created-recipe";

    interface Cases<R> {
        R CreatedRecipe(RecipeId recipeId);
    }

    <R> R match(Cases<R> cases);

    static Class<? extends UserEvent> classFor(BasicEventType eventType) {
        switch (eventType.asString()) {
            case CREATED_RECIPE:
                return UserEvents.CreatedRecipe.class;
        }

        throw new RuntimeException("Should never happen");
    }

    default BasicEventType eventType() {
        return BasicEventType.of(UserEvents.cases()
                .CreatedRecipe_("created-recipe")
                .apply(this));
    }

    @Override
    int hashCode();
    @Override
    boolean equals(Object obj);
    @Override
    String toString();
}
