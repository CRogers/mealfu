package mealfu.model.user;

import mealfu.events.MealfuEventStore;
import mealfu.model.recipe.RecipeId;
import mealfu.model.recipe.RecipeName;

import static mealfu.model.recipe.RecipeEvents.RecipeCreated;
import static mealfu.model.recipe.RecipeEvents.RecipeNameChanged;
import static mealfu.model.user.UserEvents.AddedRecipe;

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
                userId.just(AddedRecipe(recipeId)),
                RecipeCreated(userId).withId(recipeId),
                RecipeNameChanged(recipeName).withId(recipeId)
        );

        return recipeId;
    }
}
