package uk.callumr.local.aws.lambda.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableHttpConfig.class)
@JsonTypeName("http")
public interface HttpConfig extends EventConfig {
    String path();
    String method();
}
