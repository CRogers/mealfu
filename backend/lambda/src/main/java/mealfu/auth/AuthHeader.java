package mealfu.auth;

import org.immutables.value.Value;

@Value.Immutable
public interface AuthHeader {
    String bearerToken();

    static AuthHeader fromString(String bearerToken) {
        return ImmutableAuthHeader.builder()
                .bearerToken(bearerToken)
                .build();
    }
}
