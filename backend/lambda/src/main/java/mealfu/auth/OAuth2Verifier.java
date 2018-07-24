package mealfu.auth;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import mealfu.auth.config.OAuth2ProviderConfig;

import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

public class OAuth2Verifier {
    private final OAuth2ProviderConfig oAuth2ProviderConfig;
    private final JwkProvider jwkProvider;

    public OAuth2Verifier(OAuth2ProviderConfig oAuth2ProviderConfig) {
        this.oAuth2ProviderConfig = oAuth2ProviderConfig;
        this.jwkProvider = new JwkProviderBuilder(oAuth2ProviderConfig.jwkUrl())
                .cached(10, 24, TimeUnit.HOURS)
                .build();
    }

    public void verify(AuthHeader authHeader) throws AuthHeaderVerificationException {
        try {
            DecodedJWT decodedJWT = JWT.decode(authHeader.bearerToken());
            System.out.println("decodedJWT = " + decodedJWT.getAudience());
            Jwk jwk = jwkProvider.get(decodedJWT.getKeyId());
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            JWTVerifier jwtVerifier = JWT.require(algorithm)
                    .withIssuer(oAuth2ProviderConfig.issuer().asString())
                    .build();
            jwtVerifier.verify(authHeader.bearerToken());
        } catch (JWTVerificationException | JwkException e) {
            throw new AuthHeaderVerificationException(e);
        }
    }
}
