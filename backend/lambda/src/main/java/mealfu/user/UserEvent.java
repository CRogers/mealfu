package mealfu.user;

import org.derive4j.Data;
import uk.callumr.eventstore.core.BasicEventType;

@Data
public interface UserEvent {
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

    static BasicEventType eventTypeFor(UserEvent userEvent) {
        return BasicEventType.of(UserEvents.cases()
                .CreatedRecipe_("created-recipe")
                .apply(userEvent));
    }

    @Override
    int hashCode();
    @Override
    boolean equals(Object obj);
    @Override
    String toString();
}
