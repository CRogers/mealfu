package uk.callumr.local.aws.lambda;

import one.util.streamex.EntryStream;
import spark.Service;
import uk.callumr.local.aws.lambda.config.ServerlessConfig;

public class LocalAwsLambda {
    private final Service service = Service.ignite()
            .port(9876);

    public LocalAwsLambda(ServerlessConfig serverlessConfig) {
        EntryStream.of(serverlessConfig.functions())
                .values()
                .forEach(functionConfig -> {
                    functionConfig.events().forEach(httpConfig -> {
                        if (!httpConfig.method().equals("get")) {
                            throw new RuntimeException("Only GET is supported atm");
                        }

                        service.get(httpConfig.path(), (request, response) -> functionConfig.handler());
                    });
                });
    }

    public void start() {
        service.init();
    }
}
