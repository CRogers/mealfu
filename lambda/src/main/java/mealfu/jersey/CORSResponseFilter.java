package mealfu.jersey;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

public class CORSResponseFilter implements ContainerResponseFilter {
    private final String origin;

    private CORSResponseFilter(String origin) {
        this.origin = origin;
    }

    public static CORSResponseFilter allowEverything() {
        return new CORSResponseFilter("*");
    }

    public static CORSResponseFilter just(String origin) {
        return new CORSResponseFilter(origin);
    }

    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        headers.add("Access-Control-Allow-Origin", origin);
        headers.add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        headers.add("Access-Control-Allow-Headers", "X-Requested-With, Content-Type");
    }

}
