package mealfu;

import mealfu.auth.UserAuthorizer;
import mealfu.auth.config.AuthConfig;
import mealfu.config.DatabaseConfig;
import mealfu.jersey.AuthFilter;
import mealfu.jersey.CORSResponseFilter;
import mealfu.testing.TestService;
import org.glassfish.jersey.server.ResourceConfig;
import org.immutables.value.Value;
import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.cockroachdb.JdbcConnectionProvider;
import uk.callumr.eventstore.cockroachdb.PostgresEventStore;

@Value.Immutable
public abstract class Mealfu {
    protected abstract DatabaseConfig databaseConfig();
    protected abstract AuthConfig authConfig();

    @Value.Lazy
    protected UserAuthorizer userAuthorizer() {
        return new UserAuthorizer(authConfig());
    }

    @Value.Lazy
    protected EventStore eventStore() {
        JdbcConnectionProvider connectionProvider = JdbcConnectionProvider.postgres(
                databaseConfig().host(),
                databaseConfig().port(),
                databaseConfig().database())
                .username(databaseConfig().username())
                .password(databaseConfig().password())
                .build();

        return new PostgresEventStore(connectionProvider, databaseConfig().schema());
    }

    @Value.Lazy
    protected TestService testService() {
        return new TestService(eventStore(), userAuthorizer());
    }

    /**
     * @deprecated Make sure to use {@link #prodJerseyApplication()} in prod!
     */
    @Value.Lazy
    @Deprecated
    public ResourceConfig exposed_for_testing_commonJerseyApplication() {
        return new ResourceConfig()
                .register(new AuthFilter(userAuthorizer()))
                .register(testService());
    }

    @Value.Lazy
    public ResourceConfig prodJerseyApplication() {
        return new ResourceConfig(exposed_for_testing_commonJerseyApplication())
                .register(CORSResponseFilter.just("https://crogers.github.io"));
    }

    public static class Builder extends ImmutableMealfu.Builder { }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder configuredFromEnvironmentVariables() {
        return builder()
                .databaseConfig(DatabaseConfig.fromEnvironmentVariables());
    }
}
