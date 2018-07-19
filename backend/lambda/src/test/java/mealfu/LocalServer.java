package mealfu;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.configuration.ProjectName;
import com.palantir.docker.compose.configuration.ShutdownStrategy;
import com.palantir.docker.compose.connection.DockerPort;
import mealfu.config.AuthConfig;
import mealfu.config.DatabaseConfig;
import mealfu.jersey.CORSResponseFilter;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LocalServer {
    public static void main(String... args) throws IOException, InterruptedException {
        try {
            realMain();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void realMain() throws IOException, InterruptedException {
        DockerPort postgresPort = setupPostgres();

        Mealfu mealfu = Mealfu.builder()
                .databaseConfig(DatabaseConfig.builder()
                        .host(postgresPort.getIp())
                        .port(postgresPort.getExternalPort())
                        .database("mealfu")
                        .username("postgres")
                        .password("password")
                        .schema("postgres")
                        .build())
                .authConfig(AuthConfig.fromEnvironmentVariables())
                .build();

        ResourceConfig testConfig = new ResourceConfig(mealfu.exposed_for_testing_commonJerseyApplication())
                .register(CORSResponseFilter.allowEverything());


        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:" + serverPort()), testConfig);

        httpServer.start();

        Thread.sleep(99999999999L);
    }

    private static DockerPort setupPostgres() throws IOException, InterruptedException {
        DockerComposeRule dockerComposeRule = DockerComposeRule.builder()
                .file(LocalServer.class.getResource("/postgres-local-server.yml").getFile())
                .projectName(ProjectName.fromString("mealfu"))
                .shutdownStrategy(ShutdownStrategy.SKIP)
                .build();

        dockerComposeRule.before();

        Runtime.getRuntime().addShutdownHook(new Thread(dockerComposeRule::after));

        return dockerComposeRule.containers().container("postgres").port(5432);
    }

    private static int serverPort() {
        try {
            return Integer.parseInt(new String(Files.readAllBytes(Paths.get("local-server-port")), StandardCharsets.UTF_8).trim());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
