package mealfu.model.recipe;

import com.fasterxml.jackson.annotation.JsonCreator;
import mealfu.ids.MealfuEntityId;
import org.immutables.value.Value;
import uk.callumr.eventstore.core.BasicEventType;

@Value.Immutable
public abstract class RecipeId extends MealfuEntityId<RecipeEvent> {
    @Override
    public Class<? extends RecipeEvent> eventClassFor(BasicEventType eventType) {
        return RecipeEvents.typeNameToClass(eventType.asString());
    }

    @Override
    public String entityType() {
        return "recipe";
    }

    public static RecipeId of(String id) {
        return ImmutableRecipeId.builder()
                .identifier(id)
                .build();
    }

    @JsonCreator
    public static RecipeId parse(String stringId) {
        return MealfuEntityId.parse(RecipeId::of, stringId);
    }

    public static RecipeId random() {
        return MealfuEntityId.random(RecipeId::of);
    }
}

