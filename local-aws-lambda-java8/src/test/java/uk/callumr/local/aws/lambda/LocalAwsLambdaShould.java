package uk.callumr.local.aws.lambda;

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
    @Test
    public void run_a_lambda_with_a_get_method() throws IOException {
        LocalAwsLambda localAwsLambda = new LocalAwsLambda(ServerlessConfig.builder()
                .putFunctions("test", FunctionConfig.builder()
                        .handler("handler")
                        .addEvents(HttpConfig.builder()
                                .method("get")
                                .path("/foo")
                                .build())
                        .build())
                .build());

        localAwsLambda.start();

        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse response = httpClient.execute(new HttpGet("http://localhost:9876/foo"));
        String responseString = IOUtils.toString(response.getEntity().getContent());

        assertThat(responseString).isEqualTo("handler");
    }

}