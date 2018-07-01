package test.eventstore.impls;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.configuration.ProjectName;
import com.palantir.docker.compose.configuration.ShutdownStrategy;
import com.palantir.docker.compose.connection.DockerPort;
import org.junit.ClassRule;
import test.eventstore.EventStoreShould;
import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.cockroachdb.CockroachDbEventStore;

public class CockroachDbEventStoreShould extends EventStoreShould {
    @ClassRule
    public static DockerComposeRule dockerComposeRule = DockerComposeRule.builder()
            .file(EventStoreShould.class.getResource("/cockroachdb.yml").getFile())
            .projectName(ProjectName.fromString("cockroach"))
            .shutdownStrategy(ShutdownStrategy.SKIP)
            .build();

    public CockroachDbEventStoreShould() {
        super(cockroachdb());
    }

    private static EventStore cockroachdb() {
        DockerPort port = dockerComposeRule.containers()
                .container("cockroachdb")
                .port(26257);

        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/hi",
                port.getIp(),
                port.getExternalPort()
        );

        return new CockroachDbEventStore(jdbcUrl);
    }
}
