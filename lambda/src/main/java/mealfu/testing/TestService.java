package mealfu.testing;

import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.core.EntityId;
import uk.callumr.eventstore.core.EventFilters;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Path("/")
public class TestService {

    private static final AtomicLong counter = new AtomicLong(0);

    private final EventStore eventStore;

    public TestService(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Path("test")
    @GET
    public String test() {
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