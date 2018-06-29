package mealfu;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class LocalServer {
    public static void main(String... args) throws IOException, InterruptedException {
        int port = Integer.valueOf(args[0]);

        ResourceConfig testConfig = new ResourceConfig(JerseyConfig.JERSY_APPLICATION)
                .register(new CORSResponseFilter());

        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:" + port), testConfig);

        httpServer.start();

        Thread.sleep(99999999999L);
    }
}
