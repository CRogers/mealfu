package uk.callumr.local.aws.lambda.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.immutables.value.Value;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Value.Immutable
@JsonDeserialize(as = ImmutableServerlessConfig.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface ServerlessConfig {
    Map<String, FunctionConfig> functions();

    static ServerlessConfig deserializeFromFile(Path path) {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try {
            return objectMapper.readValue(path.toFile(), ServerlessConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
