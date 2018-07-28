package mealfu.model.user;

import mealfu.events.MealfuEventStore;
import mealfu.model.recipe.RecipeId;
import mealfu.model.recipe.RecipeName;
import org.junit.Ignore;
import org.junit.Test;
import uk.callumr.eventstore.InMemoryEventStore;

import static mealfu.model.recipe.RecipeEvents.RecipeCreated;
import static mealfu.model.user.UserEvents.CreatedRecipe;
import static org.assertj.core.api.Assertions.assertThat;

public class UserShould {
    private final MealfuEventStore eventStore = new MealfuEventStore(new InMemoryEventStore());
    private final UserId userId = UserId.of("dave");
    private final User user = new User(userId, eventStore);

    private final RecipeName recipeName = RecipeName.of("tasty pasta");

    @Test
    @Ignore // until we get the derive4j serialization working
    public void create_a_recipe() {
        RecipeId recipeId = user.createRecipe(recipeName);

        assertThat(eventStore.events(recipeId)).containsExactly(
                RecipeCreated(recipeName, userId)
        );

        assertThat(eventStore.events(userId)).containsExactly(
                CreatedRecipe(recipeId)
        );
    }
}