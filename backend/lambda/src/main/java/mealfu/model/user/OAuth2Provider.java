package mealfu.model.user;

public enum OAuth2Provider {
    GOOGLE;

    public String asString() {
        return this.name().toLowerCase();
    }

    public static OAuth2Provider parse(String str) {
        return OAuth2Provider.valueOf(str.toUpperCase());
    }
}
