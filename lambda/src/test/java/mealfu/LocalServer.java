package mealfu;

import mealfu.jersey.CORSResponseFilter;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class LocalServer {
    public static void main(String... args) throws IOException, InterruptedException {
        int port = Integer.valueOf(args[0]);

        Mealfu mealfu = Mealfu.init();

        ResourceConfig testConfig = new ResourceConfig(mealfu.exposed_for_testing_commonJerseyApplication())
                .register(CORSResponseFilter.allowEverything());

        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:" + port), testConfig);

        httpServer.start();

        Thread.sleep(99999999999L);
    }
}
