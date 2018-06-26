package mealfu;

import com.serverless.JerseyConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.io.IOException;
import java.net.URI;

public class LocalServer {
    public static void main(String... args) throws IOException, InterruptedException {
        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:7787"), JerseyConfig.JERSY_APPLICATION);

        httpServer.start();

        Thread.sleep(99999999999L);
    }
}
