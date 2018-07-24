package mealfu.auth.config;

public enum ProdAuthConfig {
    ;

    public static final AuthConfig PROD_AUTH_CONFIG = AuthConfig.builder()
            .addOauth2Providers(Providers.GOOGLE)
            .build();
}
