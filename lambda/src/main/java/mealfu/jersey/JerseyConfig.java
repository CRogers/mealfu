package mealfu.jersey;

import org.glassfish.jersey.server.ResourceConfig;

public enum JerseyConfig {
    ;

    public static final ResourceConfig COMMON_JERSY_APPLICATION = new ResourceConfig()
            .packages("mealfu.servlets");

    public static final ResourceConfig PROD_JERSEY_APPLICATION = new ResourceConfig()
            .register(CORSResponseFilter.just("http://crogers.github.io"));
}
