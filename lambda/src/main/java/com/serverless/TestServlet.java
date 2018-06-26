package com.serverless;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class TestServlet {

    @Path("test")
    @GET
    public String test() {
        return "test";
    }
}
