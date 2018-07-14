package mealfu;

import mealfu.jersey.CORSResponseFilter;
import mealfu.testing.TestService;
import org.glassfish.jersey.server.ResourceConfig;
import org.immutables.value.Value;
import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.cockroachdb.JdbcConnectionProvider;
import uk.callumr.eventstore.cockroachdb.PostgresEventStore;

import java.util.Optional;

@Value.Immutable
public abstract class Mealfu {
    private static final String DB_HOST = "DB_HOST";
    private static final String DB_PORT = "DB_PORT";
    private static final String DB_DATABASE = "DB_DATABASE";
    private static final String DB_USERNAME = "DB_USERNAME";
    private static final String DB_PASSWORD = "DB_PASSWORD";
    private static final String DB_SCHEMA = "DB_SCHEMA";

    @Value.Lazy
    protected EventStore eventStore() {
        JdbcConnectionProvider connectionProvider = JdbcConnectionProvider.postgres(
                environmentVariable(DB_HOST),
                Integer.parseInt(environmentVariable(DB_PORT)),
                environmentVariable(DB_DATABASE))
                .username(environmentVariable(DB_USERNAME))
                .password(environmentVariable(DB_PASSWORD))
                .build();

        return new PostgresEventStore(connectionProvider, environmentVariable(DB_SCHEMA));
    }

    @Value.Lazy
    protected TestService testService() {
        return new TestService(eventStore());
    }

    /**
     * @deprecated Make sure to use {@link #prodJerseyApplication()} in prod!
     */
    @Value.Lazy
    @Deprecated
    public ResourceConfig exposed_for_testing_commonJerseyApplication() {
        return new ResourceConfig()
                .register(testService());
    }

    @Value.Lazy
    public ResourceConfig prodJerseyApplication() {
        return new ResourceConfig(exposed_for_testing_commonJerseyApplication())
                .register(CORSResponseFilter.just("https://crogers.github.io"));
    }

    private static String environmentVariable(String variableName) {
        return Optional.ofNullable(System.getenv(variableName))
                .orElseThrow(() -> new IllegalStateException(variableName + " environment variable not set!"));
    }

    public static Mealfu init() {
        return ImmutableMealfu.builder().build();
    }
}
