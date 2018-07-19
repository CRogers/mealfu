package mealfu.config;

import java.util.Optional;

public enum ConfigUtils {
    ;

    public static String environmentVariable(String variableName) {
        return Optional.ofNullable(System.getenv(variableName))
                .orElseThrow(() -> new IllegalStateException(variableName + " environment variable not set!"));
    }
}
