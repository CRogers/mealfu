package mealfu.auth;
import org.immutables.value.Value;

@Value.Immutable
public interface Audience {
    String asString();

    class Builder extends ImmutableAudience.Builder { }

    static Builder builder() {
        return new Builder();
    }

    static Audience of(String audience) {
        return builder()
                .asString(audience)
                .build();
    }
}

