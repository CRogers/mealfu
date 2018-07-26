package mealfu.user;

import uk.callumr.eventstore.EventStore;

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
                RecipeCreatedByUser.withId(recipeId).by(userId));

        return recipeId;
    }
}
