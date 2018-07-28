package mealfu.model.user;

import mealfu.events.MealfuEventStore;
import mealfu.model.recipe.RecipeId;
import mealfu.model.recipe.RecipeName;

import static mealfu.events.EnglishHelpers.by;
import static mealfu.events.EnglishHelpers.its;
import static mealfu.events.EnglishHelpers.to;
import static mealfu.model.recipe.Recipe.Created;
import static mealfu.model.recipe.Recipe.NameChanged;
import static mealfu.model.user.UserEvents.AddedRecipe;

public class User {
    private final UserId user;
    private final MealfuEventStore eventStore;

    public User(UserId userId, MealfuEventStore eventStore) {
        this.user = userId;
        this.eventStore = eventStore;
    }

    public RecipeId createRecipe(RecipeName recipeName) {
        RecipeId recipe = RecipeId.random();

        eventStore.addEvents(
                recipe.was(Created(by(user))),
                recipe.had(its(NameChanged(to(recipeName)))),
                user.just(AddedRecipe(recipe))
        );

        return recipe;
    }
}
