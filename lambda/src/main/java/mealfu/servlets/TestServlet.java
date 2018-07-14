package mealfu.servlets;

import uk.callumr.eventstore.EventStore;
import uk.callumr.eventstore.cockroachdb.JdbcConnectionProvider;
import uk.callumr.eventstore.cockroachdb.PostgresEventStore;
import uk.callumr.eventstore.core.EntityId;
import uk.callumr.eventstore.core.EventFilters;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Path("/")
public class TestServlet {

    private static AtomicLong counter = new AtomicLong(0);

    @Path("test")
    @GET
    public String test() {
        return "The current time is " + ZonedDateTime.now().toString();
    }

    @Path("db")
    @GET
    public String db() {
        JdbcConnectionProvider connectionProvider = JdbcConnectionProvider.postgres(
                environmentVariable("DB_HOST"),
                Integer.parseInt(environmentVariable("DB_PORT")),
                environmentVariable("DB_DATABASE"))
                .username(environmentVariable("DB_USERNAME"))
                .password(environmentVariable("DB_PASSWORD"))
                .build();

        EventStore eventStore = new PostgresEventStore(connectionProvider, environmentVariable("DB_SCHEMA"));

        long numEvents = eventStore.events(EventFilters.forEntity(EntityId.random()))
                .count();

        return numEvents + " events";
    }

    @Path("static")
    @GET
    public long staticTest() {
        return counter.getAndIncrement();
    }

    private String environmentVariable(String variableName) {
        return Optional.ofNullable(System.getenv(variableName))
                .orElseThrow(() -> new IllegalStateException(variableName + " environment variable not set!"));
    }
}
