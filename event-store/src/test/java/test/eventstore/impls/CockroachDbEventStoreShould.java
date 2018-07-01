package test.eventstore.impls;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.configuration.ProjectName;
import com.palantir.docker.compose.configuration.ShutdownStrategy;
import com.palantir.docker.compose.connection.DockerPort;
import org.apache.commons.lang3.RandomStringUtils;
import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.ClassRule;
import test.eventstore.EventStoreShould;
import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.cockroachdb.CockroachDbEventStore;
import uk.callumr.eventstore.cockroachdb.JdbcConnectionProvider;
import uk.callumr.eventstore.jooq.JooqUtils;

public class CockroachDbEventStoreShould extends EventStoreShould {
    @ClassRule
    public static DockerComposeRule dockerComposeRule = DockerComposeRule.builder()
            .file(EventStoreShould.class.getResource("/postgres.yml").getFile())
            .projectName(ProjectName.fromString("postgresev"))
            .shutdownStrategy(ShutdownStrategy.SKIP)
            .build();

    public CockroachDbEventStoreShould() {
        super(cockroachdb());
    }

    private static EventStore cockroachdb() {
        ConnectionProvider testConnectionProvider = connectionProviderForDatabase("postgres");

        String schema = randomSchemaName();
        DSL.using(testConnectionProvider, SQLDialect.POSTGRES)
                .query("create schema " + schema)
                .execute();

        return new CockroachDbEventStore(testConnectionProvider, schema);
    }

    private static String randomSchemaName() {
        return "s" + RandomStringUtils.randomAlphanumeric(6);
    }

    private static ConnectionProvider connectionProviderForDatabase(String database) {
        DockerPort port = dockerComposeRule.containers()
                .container("postgres")
                .port(5432);

        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/" + database,
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
