package mealfu.user;

import mealfu.events.MealfuEvent;
import org.derive4j.Data;
import uk.callumr.eventstore.core.BasicEventType;

@Data
public interface RecipeEvent extends MealfuEvent<RecipeId> {
    String RECIPE_CREATED = "recipe-created";

    interface Cases<R> {
        R RecipeCreated(RecipeName recipeName, UserId creator);
    }

    <R> R match(Cases<R> cases);

    static Class<? extends RecipeEvent> classFor(BasicEventType eventType) {
        switch (eventType.asString()) {
            case RECIPE_CREATED:
                return RecipeEvents.RecipeCreated.class;
        }

        throw new RuntimeException("Should never happen");
    }

    default BasicEventType eventType() {
        return BasicEventType.of(RecipeEvents.cases()
                .RecipeCreated_(RECIPE_CREATED)
                .apply(this));
    }

    @Override
    int hashCode();
    @Override
    boolean equals(Object obj);
    @Override
    String toString();
}
