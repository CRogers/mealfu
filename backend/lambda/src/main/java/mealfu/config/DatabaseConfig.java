package mealfu.config;

import org.immutables.value.Value;

import static mealfu.config.ConfigUtils.environmentVariable;

@Value.Immutable
public abstract class DatabaseConfig {
    private static final String HOST = "DB_HOST";
    private static final String PORT = "DB_PORT";
    private static final String DATABASE = "DB_DATABASE";
    private static final String USERNAME = "DB_USERNAME";
    private static final String PASSWORD = "DB_PASSWORD";
    private static final String SCHEMA = "DB_SCHEMA";

    public abstract String host();
    public abstract int port();
    public abstract String database();
    public abstract String username();
    public abstract String password();
    public abstract String schema();

    public static class Builder extends ImmutableDatabaseConfig.Builder { }

    public static Builder builder() {
        return new Builder();
    }

    public static DatabaseConfig fromEnvironmentVariables() {
        return builder()
                .host(environmentVariable(HOST))
                .port(Integer.parseInt(environmentVariable(PORT)))
                .database(environmentVariable(DATABASE))
                .username(environmentVariable(USERNAME))
                .password(environmentVariable(PASSWORD))
                .schema(environmentVariable(SCHEMA))
                .build();
    }

}
