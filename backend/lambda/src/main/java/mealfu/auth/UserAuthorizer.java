package mealfu.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import mealfu.auth.config.AuthConfig;
import mealfu.auth.config.OAuth2ProviderConfig;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserAuthorizer {
    private final Map<Audience, OAuth2Verifier> verifiers;

    public UserAuthorizer(AuthConfig authConfig) {
        this.verifiers = authConfig.oauth2Providers()
                .stream()
                .collect(Collectors.toMap(OAuth2ProviderConfig::audience, OAuth2Verifier::new));
    }

    public void verifyUser(AuthHeader authHeader) throws AuthHeaderVerificationException {
        DecodedJWT decodedJWT = JWT.decode(authHeader.bearerToken());
        OAuth2Verifier jwk = forAudienceAndKey(Audience.of(decodedJWT.getAudience().get(0)));
        jwk.verify(authHeader);
    }

    private OAuth2Verifier forAudienceAndKey(Audience audience) {
        return Optional.ofNullable(verifiers.get(audience))
                .orElseThrow(() -> new IllegalArgumentException("No OAuth2Provider for audience " + audience));
    }
}
