package mealfu.auth.config;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface AuthConfig {
    List<OAuth2ProviderConfig> oauth2Providers();

    class Builder extends ImmutableAuthConfig.Builder { }

    static Builder builder() {
        return new Builder();
    }
}

