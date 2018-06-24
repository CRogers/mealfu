package uk.callumr.local.aws.lambda;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import one.util.streamex.EntryStream;
import spark.Service;
import uk.callumr.local.aws.lambda.config.ServerlessConfig;

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
}
