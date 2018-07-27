package mealfu.user;
import mealfu.events.MealfuEvent;
import org.immutables.value.Value;
import uk.callumr.eventstore.core.BasicEventType;

@Value.Immutable
public interface RecipeCreatedByUser extends MealfuEvent<UserId> {
    BasicEventType TYPE = BasicEventType.of("recipe-created-by-user");

    RecipeId recipeId();

    default BasicEventType eventType() {
        return TYPE;
    }

    class Builder extends ImmutableRecipeCreatedByUser.Builder { }

    static Builder builder() {
        return new Builder();
    }

    static RecipeCreatedByUser withId(RecipeId recipeId) {
        return builder()
                .recipeId(recipeId)
                .build();
    }
}

