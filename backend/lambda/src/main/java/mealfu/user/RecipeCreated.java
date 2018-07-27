package mealfu.user;
import mealfu.events.MealfuEvent;
import org.immutables.value.Value;
import uk.callumr.eventstore.core.BasicEventType;

@Value.Immutable
public interface RecipeCreated extends MealfuEvent<RecipeId> {
    BasicEventType TYPE = BasicEventType.of("recipe-created");

    RecipeName recipeName();
    UserId creator();

    default BasicEventType eventType() {
        return TYPE;
    }

    class Builder extends ImmutableRecipeCreated.Builder { }

    static Builder builder() {
        return new Builder();
    }
}

