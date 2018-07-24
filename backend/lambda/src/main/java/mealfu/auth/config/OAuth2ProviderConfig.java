package mealfu.auth.config;

import mealfu.auth.Audience;
import mealfu.auth.Issuer;
import org.immutables.value.Value;

import java.net.MalformedURLException;
import java.net.URL;

@Value.Immutable
public interface OAuth2ProviderConfig {
    Issuer issuer();
    URL jwkUrl();
    Audience audience();

    class Builder extends ImmutableOAuth2ProviderConfig.Builder {
        public Builder jwkUrl(String url) {
            try {
                jwkUrl(new URL(url));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            return this;
        }
    }

    static Builder builder() {
        return new Builder();
    }
}
