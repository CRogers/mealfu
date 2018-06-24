package uk.callumr.local.aws.lambda.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonDeserialize(as = ImmutableFunctionConfig.class)
public interface FunctionConfig {
    String handler();
    List<HttpConfig> events();

    static ImmutableFunctionConfig.Builder builder() {
        return ImmutableFunctionConfig.builder();
    }
}
