package mealfu.auth;
import org.immutables.value.Value;

@Value.Immutable
public interface Issuer {
    String asString();

    class Builder extends ImmutableIssuer.Builder { }

    static Builder builder() {
        return new Builder();
    }

    static Issuer of(String issuer) {
        return builder()
                .asString(issuer)
                .build();
    }
}

