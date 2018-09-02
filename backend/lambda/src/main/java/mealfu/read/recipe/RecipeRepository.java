package mealfu.read.recipe;

import mealfu.events.MealfuEventStore;
import mealfu.model.recipe.RecipeEvents.Created;
import mealfu.model.recipe.RecipeEvents.NameChanged;
import mealfu.model.recipe.RecipeId;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static mealfu.model.recipe.RecipeEvents.caseOf;

public class RecipeRepository {
    private final MealfuEventStore eventStore;

    public RecipeRepository(MealfuEventStore eventStore) {
        this.eventStore = eventStore;
    }

    public Stream<Recipe> recipes(Iterable<RecipeId> recipeIds) {
        return eventStore.eventsFor(recipeIds, Created.class, NameChanged.class)
                .mapKeyValue((recipeId, events) -> events.foldLeft(Recipe.builder(), (builder, event) ->
                        caseOf(event)
                        .Created(builder::createdBy)
                        .NameChanged(builder::recipeName))
                        .build());
    }

    private static <T, R> Function<T, UnaryOperator<R>> appliedTo(BiFunction<T, R, R> biFunction) {
        return value -> r -> biFunction.apply(value, r);
    }
}
