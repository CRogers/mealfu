package mealfu.write.user;

import mealfu.events.MealfuEventStore;
import mealfu.model.recipe.RecipeId;
import mealfu.model.recipe.RecipeName;
import mealfu.model.user.UserId;
import org.junit.Test;
import uk.callumr.eventstore.InMemoryEventStore;

import static mealfu.model.recipe.RecipeEvents.Created;
import static mealfu.model.recipe.RecipeEvents.NameChanged;
import static mealfu.model.user.UserEvents.AddedRecipe;
import static org.assertj.core.api.Assertions.assertThat;

public class UserShould {
    private final MealfuEventStore eventStore = new MealfuEventStore(new InMemoryEventStore());
    private final UserId userId = UserId.of("dave");
    private final User user = new User(userId, eventStore);

    private final RecipeName recipeName = RecipeName.of("tasty pasta");

    @Test
    public void create_a_recipe() {
        RecipeId recipeId = user.createRecipe(recipeName);

        assertThat(eventStore.eventsFor(recipeId)).containsExactly(
                Created(userId),
                NameChanged(recipeName)
        );

        assertThat(eventStore.eventsFor(userId)).containsExactly(
                AddedRecipe(recipeId)
        );
    }
}