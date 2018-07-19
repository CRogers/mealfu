package mealfu.config;

import org.immutables.value.Value;

import static mealfu.config.ConfigUtils.environmentVariable;

@Value.Immutable
public abstract class AuthConfig {
    private static final String GOOGLE_CLIENT_SECRET = "GOOGLE_CLIENT_SECRET";

    public abstract String googleClientSecret();

    public static class Builder extends ImmutableAuthConfig.Builder { }

    public static Builder builder() {
        return new Builder();
    }

    public static AuthConfig fromEnvironmentVariables() {
        return builder()
                .googleClientSecret(environmentVariable(GOOGLE_CLIENT_SECRET))
                .build();
    }
}
