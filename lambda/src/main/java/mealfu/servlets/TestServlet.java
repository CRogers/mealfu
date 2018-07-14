package mealfu.servlets;

import com.google.common.base.Suppliers;
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
import java.util.function.Supplier;

@Path("/")
public class TestServlet {

    private static final AtomicLong counter = new AtomicLong(0);

    private static final Supplier<EventStore> EVENT_STORE = Suppliers.memoize(() -> {
        JdbcConnectionProvider connectionProvider = JdbcConnectionProvider.postgres(
                environmentVariable("DB_HOST"),
                Integer.parseInt(environmentVariable("DB_PORT")),
                environmentVariable("DB_DATABASE"))
                .username(environmentVariable("DB_USERNAME"))
                .password(environmentVariable("DB_PASSWORD"))
                .build();

        return new PostgresEventStore(connectionProvider, environmentVariable("DB_SCHEMA"));
    });

    @Path("test")
    @GET
    public String test() {
        return "The current time is " + ZonedDateTime.now().toString();
    }

    @Path("db")
    @GET
    public String db() {
        long numEvents = EVENT_STORE.get().events(EventFilters.forEntity(EntityId.random()))
                .count();

        return numEvents + " events";
    }

    @Path("static")
    @GET
    public long staticTest() {
        return counter.getAndIncrement();
    }

    private static String environmentVariable(String variableName) {
        return Optional.ofNullable(System.getenv(variableName))
                .orElseThrow(() -> new IllegalStateException(variableName + " environment variable not set!"));
    }
}
