package mealfu.user;
import mealfu.events.MealfuEvent;
import org.immutables.value.Value;
import uk.callumr.eventstore.core.EventType;

@Value.Immutable
public interface RecipeCreated extends MealfuEvent<RecipeId> {
    EventType TYPE = EventType.of("recipe-created");

    RecipeName recipeName();

    default EventType eventType() {
        return TYPE;
    }

    class Builder extends ImmutableRecipeCreated.Builder { }

    static Builder builder() {
        return new Builder();
    }
}

