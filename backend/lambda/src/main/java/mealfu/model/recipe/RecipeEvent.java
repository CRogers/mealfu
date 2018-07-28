package mealfu.model.recipe;

import mealfu.events.MealfuEvent;
import mealfu.model.user.UserId;
import org.derive4j.Data;
import uk.callumr.eventstore.core.BasicEventType;

@Data
public abstract class RecipeEvent implements MealfuEvent<RecipeId> {
    private static final String RECIPE_CREATED = "recipe-created";
    private static final String RECIPE_NAME_CHANGED = "recipe-name-changed";

    interface Cases<R> {
        R Created(UserId creator);
        R NameChanged(RecipeName recipeName);
    }

    public abstract <R> R match(Cases<R> cases);

    static Class<? extends RecipeEvent> classFor(BasicEventType eventType) {
        switch (eventType.asString()) {
            case RECIPE_CREATED:
                return RecipeEvents.Created.class;
            case RECIPE_NAME_CHANGED:
                return RecipeEvents.NameChanged.class;
        }

        throw new RuntimeException("Should never happen");
    }

    public final BasicEventType eventType() {
        return BasicEventType.of(RecipeEvents.caseOf(this)
                .Created_(RECIPE_CREATED)
                .NameChanged_(RECIPE_NAME_CHANGED));
    }

    @Override
    public abstract int hashCode();
    @Override
    public abstract boolean equals(Object obj);
    @Override
    public abstract String toString();
}
