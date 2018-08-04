package mealfu.read.user;

import mealfu.events.MealfuEventStore;
import mealfu.model.recipe.RecipeId;
import mealfu.model.user.UserEvents.AddedRecipe;
import mealfu.model.user.UserId;

import java.util.stream.Stream;

public class User {
    private final UserId userId;
    private final MealfuEventStore eventStore;

    public User(UserId userId, MealfuEventStore eventStore) {
        this.userId = userId;
        this.eventStore = eventStore;
    }

    public Stream<RecipeId> allRecipes() {
        return eventStore.eventsFor(userId, AddedRecipe.class)
                .map(AddedRecipe::recipeId);
    }
}
