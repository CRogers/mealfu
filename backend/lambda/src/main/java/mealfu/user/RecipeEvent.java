package mealfu.user;

import mealfu.events.MealfuEvent;
import org.derive4j.Data;
import uk.callumr.eventstore.core.BasicEventType;

@Data
public abstract class RecipeEvent implements MealfuEvent<RecipeId> {
    private static final String RECIPE_CREATED = "recipe-created";

    interface Cases<R> {
        R RecipeCreated(RecipeName recipeName, UserId creator);
    }

    public abstract <R> R match(Cases<R> cases);

    static Class<? extends RecipeEvent> classFor(BasicEventType eventType) {
        switch (eventType.asString()) {
            case RECIPE_CREATED:
                return RecipeEvents.RecipeCreated.class;
        }

        throw new RuntimeException("Should never happen");
    }

    public final BasicEventType eventType() {
        return BasicEventType.of(RecipeEvents.cases()
                .RecipeCreated_(RECIPE_CREATED)
                .apply(this));
    }

    @Override
    public abstract int hashCode();
    @Override
    public abstract boolean equals(Object obj);
    @Override
    public abstract String toString();
}
