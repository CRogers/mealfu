package mealfu.user;
import org.immutables.value.Value;

@Value.Immutable
public interface RecipeName {
    String asString();

    class Builder extends ImmutableRecipeName.Builder { }

    static Builder builder() {
        return new Builder();
    }
    
    static RecipeName of(String RecipeName) {
        return builder().asString(RecipeName).build();
    }
}

