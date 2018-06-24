package uk.callumr.local.aws.lambda.config;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerlessConfigShould {
    @Test
    public void deserialize_a_valid_serverless_yml() {
        Path testServerlessFile = Paths.get(this.getClass().getResource("/test-serverless.yml").getPath());
        ServerlessConfig deserialized = ServerlessConfig.deserializeFromFile(testServerlessFile);
        System.out.println("deserialized = " + deserialized);
    }

}