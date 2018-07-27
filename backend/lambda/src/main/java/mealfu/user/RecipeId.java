package mealfu.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import mealfu.ids.MealfuEntityId;
import org.immutables.value.Value;
import uk.callumr.eventstore.core.BasicEventType;

@Value.Immutable
public interface RecipeId extends MealfuEntityId<RecipeCreatedByUser> {
    String identifier();

    @Override
    default Class<? extends RecipeCreatedByUser> eventClassFor(BasicEventType eventType) {
        return RecipeCreatedByUser.class;
    }

    @Override
    default String entityType() {
        return "recipe";
    }

    static RecipeId of(String id) {
        return ImmutableRecipeId.builder()
                .identifier(id)
                .build();
    }

    @JsonCreator
    static RecipeId parse(String stringId) {
        return MealfuEntityId.parse(RecipeId::of, stringId);
    }

    static RecipeId random() {
        return MealfuEntityId.random(RecipeId::of);
    }
}

