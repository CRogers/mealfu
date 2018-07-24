package mealfu.auth.config;

import mealfu.auth.Audience;
import mealfu.auth.Issuer;

public enum Providers {
    ;

    public static final OAuth2ProviderConfig GOOGLE = OAuth2ProviderConfig.builder()
            .jwkUrl("https://www.googleapis.com/oauth2/v3/certs")
            .issuer(Issuer.of("https://accounts.google.com"))
            .audience(Audience.of("304581327654-kiauhniedmfphqua8rnlpsmhg9mcumq2.apps.googleusercontent.com"))
            .build();
}
