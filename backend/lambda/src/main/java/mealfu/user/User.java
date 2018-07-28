package mealfu.user;

import uk.callumr.eventstore.EventStore;

import static mealfu.user.RecipeEvents.RecipeCreated;
import static mealfu.user.UserEvents.CreatedRecipe;

public class User {
    private final UserId userId;
    private final EventStore eventStore;

    public User(UserId userId, EventStore eventStore) {
        this.userId = userId;
        this.eventStore = eventStore;
    }

    public RecipeId createRecipe(RecipeName recipeName) {
        RecipeId recipeId = RecipeId.random();

        eventStore.addEvents(
                userId.just(CreatedRecipe(recipeId)),
                RecipeCreated(recipeName, userId).withId(recipeId)
        );

        return recipeId;
    }
}
