package mealfu;

import org.glassfish.jersey.server.ResourceConfig;

public enum JerseyConfig {
    ;

    public static final ResourceConfig JERSY_APPLICATION = new ResourceConfig()
            .packages("mealfu.servlets");
}
