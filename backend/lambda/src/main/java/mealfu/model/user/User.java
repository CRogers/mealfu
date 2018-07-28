package mealfu.model.user;

import mealfu.events.MealfuEventStore;
import mealfu.model.recipe.RecipeId;
import mealfu.model.recipe.RecipeName;

import static mealfu.model.recipe.RecipeEvents.RecipeCreated;
import static mealfu.model.user.UserEvents.CreatedRecipe;

public class User {
    private final UserId userId;
    private final MealfuEventStore eventStore;

    public User(UserId userId, MealfuEventStore eventStore) {
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
