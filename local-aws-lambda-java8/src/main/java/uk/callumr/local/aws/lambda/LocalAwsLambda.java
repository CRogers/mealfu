package uk.callumr.local.aws.lambda;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import one.util.streamex.EntryStream;
import spark.Service;
import uk.callumr.local.aws.lambda.config.ServerlessConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalAwsLambda {
    private final Service service = Service.ignite();

    public LocalAwsLambda(int port, ServerlessConfig serverlessConfig) {
        service.port(port);

        EntryStream.of(serverlessConfig.functions())
                .values()
                .forEach(functionConfig -> {
                    functionConfig.events().forEach(httpConfig -> {
                        if (!httpConfig.method().equals("get")) {
                            throw new RuntimeException("Only GET is supported atm");
                        }

                        service.get(httpConfig.path(), (request, response) -> {
                            Class<?> handlerClass = Class.forName(functionConfig.handler());
                            if (!RequestHandler.class.isAssignableFrom(handlerClass)) {
                                throw new RuntimeException("Only RequestHandler is supported atm");
                            }

                            return ((RequestHandler<String, String>) handlerClass.newInstance())
                                    .handleRequest("blah", new DummyContext());
                        });
                    });
                });
    }

    public void start() {
        service.init();
    }

    public String urlBase() {
        return "http://localhost:" + service.port();
    }

    public static void main(String... args) throws InterruptedException {
        Path serverlessPath = Paths.get(args[0]);
        int port = Integer.valueOf(args[1]);
        ServerlessConfig serverlessConfig = ServerlessConfig.deserializeFromFile(serverlessPath);
        LocalAwsLambda localAwsLambda = new LocalAwsLambda(port, serverlessConfig);
        localAwsLambda.start();

        System.out.println("Started local aws lambda server at " + localAwsLambda.urlBase());

        Thread.sleep(9999999999999L);
    }
}
