package test.eventstore.impls;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.configuration.ProjectName;
import com.palantir.docker.compose.configuration.ShutdownStrategy;
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.SuccessOrFailure;
import org.apache.commons.lang3.RandomStringUtils;
import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.ClassRule;
import test.eventstore.EventStoreShould;
import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.cockroachdb.JdbcConnectionProvider;
import uk.callumr.eventstore.jooq.JooqUtils;

import java.sql.Connection;

public class PostgresEventStoreShould extends EventStoreShould {
    private static final String DATABASE = "postgres";
    private static final String CONTAINER = "postgres";
    private static final int PORT = 5432;

    @ClassRule
    public static DockerComposeRule dockerComposeRule = DockerComposeRule.builder()
            .file(EventStoreShould.class.getResource("/postgres.yml").getFile())
            .projectName(ProjectName.fromString("postgresev"))
            .shutdownStrategy(ShutdownStrategy.SKIP)
            .waitingForService("postgres", container -> {
                try (Connection connection = connectionProviderForPort(container.port(PORT)).acquire()) {
                    connection.createStatement().executeQuery("select 1");
                } catch (Exception e) {
                    e.printStackTrace();
                    SuccessOrFailure.failureWithCondensedException("Could not connect to database", e);
                }
                return SuccessOrFailure.success();
            })
            .build();

    public PostgresEventStoreShould() {
        super(postgres());
    }

    private static EventStore postgres() {
        ConnectionProvider testConnectionProvider = connectionProviderForDatabase();

        String schema = randomSchemaName();
        DSL.using(testConnectionProvider, SQLDialect.POSTGRES)
                .query("create schema " + schema)
                .execute();

        return new uk.callumr.eventstore.cockroachdb.PostgresEventStore(testConnectionProvider, schema);
    }

    private static String randomSchemaName() {
        return "s" + RandomStringUtils.randomAlphanumeric(6);
    }

    private static ConnectionProvider connectionProviderForDatabase() {
        DockerPort port = dockerComposeRule.containers()
                .container(CONTAINER)
                .port(PORT);

        return connectionProviderForPort(port);
    }

    private static ConnectionProvider connectionProviderForPort(DockerPort port) {
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/" + DATABASE,
                port.getIp(),
                port.getExternalPort()
        );

        return JdbcConnectionProvider.builder()
                .jdbcUrl(jdbcUrl)
                .username("postgres")
                .password("password")
                .build();
    }

    static {
        JooqUtils.noLogo();
    }
}
