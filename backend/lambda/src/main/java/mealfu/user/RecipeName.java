package mealfu.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
@JsonDeserialize(as = ImmutableRecipeName.class)
public interface RecipeName {
    @JsonValue
    String asString();

    @JsonCreator
    static RecipeName of(String RecipeName) {
        return ImmutableRecipeName.builder().asString(RecipeName).build();
    }
}

