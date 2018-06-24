package uk.callumr.local.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import spark.utils.IOUtils;
import uk.callumr.local.aws.lambda.config.FunctionConfig;
import uk.callumr.local.aws.lambda.config.HttpConfig;
import uk.callumr.local.aws.lambda.config.ServerlessConfig;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalAwsLambdaShould {
    private static final String CONSTANT_GET = "constant-get";

    public static class ConstantGetHandler implements RequestHandler<String, String> {

        @Override
        public String handleRequest(String input, Context context) {
            return CONSTANT_GET;
        }
    }

    @Test
    public void run_a_lambda_with_a_get_method() throws IOException {
        String path = "/foo";

        LocalAwsLambda localAwsLambda = new LocalAwsLambda(0, ServerlessConfig.builder()
                .putFunctions("test", FunctionConfig.builder()
                        .handler(ConstantGetHandler.class.getName())
                        .addEvents(HttpConfig.builder()
                                .method("get")
                                .path(path)
                                .build())
                        .build())
                .build());

        localAwsLambda.start();

        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse response = httpClient.execute(new HttpGet(localAwsLambda.urlBase() + path));
        String responseString = IOUtils.toString(response.getEntity().getContent());

        assertThat(responseString).isEqualTo(CONSTANT_GET);
    }

}