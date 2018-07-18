package uk.callumr.eventstore.cockroachdb;

import org.immutables.value.Value;
import org.jooq.ConnectionProvider;
import org.jooq.exception.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Value.Immutable
public abstract class JdbcConnectionProvider implements ConnectionProvider {
    protected abstract String jdbcUrl();
    protected abstract String username();
    protected abstract String password();

    @Override
    public Connection acquire() throws DataAccessException {
        try {
            return DriverManager.getConnection(jdbcUrl(), username(), password());
        } catch (SQLException e) {
            throw new DataAccessException("could not open connection", e);
        }
    }

    @Override
    public void release(Connection connection) throws DataAccessException {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new DataAccessException("could not close connection", e);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ImmutableJdbcConnectionProvider.Builder {}

    public static Builder postgres(String host, int port, String database) {
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/" + database,
                host,
                port
        );

        return JdbcConnectionProvider.builder()
                .jdbcUrl(jdbcUrl);
    }
}
