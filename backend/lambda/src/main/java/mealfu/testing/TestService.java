package mealfu.testing;

import mealfu.auth.AuthHeader;
import mealfu.auth.UserAuthorizer;
import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.core.EntityId;
import uk.callumr.eventstore.core.EventFilters;

import javax.ws.rs.*;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Path("/")
public class TestService {

    private static final AtomicLong counter = new AtomicLong(0);

    private final EventStore eventStore;
    private final UserAuthorizer userAuthorizer;

    public TestService(EventStore eventStore, UserAuthorizer userAuthorizer) {
        this.eventStore = eventStore;
        this.userAuthorizer = userAuthorizer;
    }

    @Path("test")
    @GET
    public String test(@HeaderParam("Authorization") AuthHeader authHeader) {
        userAuthorizer.verifyUser(authHeader);
        return "The current time is " + ZonedDateTime.now().toString();
    }

    @Path("db")
    @GET
    public String db() {
        long numEvents = eventStore.events(EventFilters.forEntity(EntityId.random()))
                .count();

        return numEvents + " events";
    }

    @Path("static")
    @GET
    public long staticTest() {
        return counter.getAndIncrement();
    }

}
