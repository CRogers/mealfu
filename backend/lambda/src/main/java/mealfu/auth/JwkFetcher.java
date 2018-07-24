package mealfu.auth;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import mealfu.auth.config.OAuth2ProviderConfig;

import java.util.concurrent.TimeUnit;

public class JwkFetcher {
    private final JwkProvider jwkProvider;

    public JwkFetcher(OAuth2ProviderConfig oAuth2ProviderConfig) {
        this.jwkProvider = new JwkProviderBuilder(oAuth2ProviderConfig.jwkUrl())
                .cached(10, 24, TimeUnit.HOURS)
                .build();
    }


}
