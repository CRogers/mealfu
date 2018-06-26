package mealfu.servlets;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.time.ZonedDateTime;

@Path("/")
public class TestServlet {

    @Path("test")
    @GET
    public String test() {
        return "The current time is " + ZonedDateTime.now().toString();
    }
}
