package mealfu.user;

import org.junit.Test;
import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.InMemoryEventStore;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.callumr.eventstore.core.EventFilters.forEntity;

public class UserShould {
    private final EventStore eventStore = new InMemoryEventStore();
    private final UserId userId = UserId.of("dave");
    private final User user = new User(userId, eventStore);

    private final RecipeName recipeName = RecipeName.of("tasty pasta");

    @Test
    public void create_a_recipe() {
        RecipeId recipeId = user.createRecipe(recipeName);

        assertThat(eventStore.events(forEntity(recipeId))).containsExactly();
    }
}