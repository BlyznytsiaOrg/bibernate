package io.github.blyznytsiaorg.bibernate.connectionpool;

import io.github.blyznytsiaorg.bibernate.exception.BibernateDataSourceException;
import io.github.blyznytsiaorg.bibernate.exception.ConnectionPoolException;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * BibernateDataSource is an implementation of the DataSource interface that provides connections to a database.
 * It manages a connection pool and allows clients to obtain connections and release them when done.
 *
 * @author Blyzhnytsia Team
 * @see BibernateDataSource
 * @since 1.0
 */
@Slf4j
public class BibernateDataSource implements DataSource {
    public static final String METHOD_IS_NOT_SUPPORTED = "Method is not supported";

    /**
     * The connection pool that manages connections to the database.
     */
    private final Queue<Connection> connectionPool = new ConcurrentLinkedQueue<>();

    /**
     * Constructs a new BibernateDataSource with the given configuration.
     *
     * @param config the configuration for this data source
     * @throws BibernateDataSourceException if an error occurs while creating connections
     */
    public BibernateDataSource(BibernateDatasSourceConfig config) {
        log.debug("Starting Bibernate Datasource ...");
        for (int i = 0; i < config.getMaximumPoolSize(); i++) {
            Connection connection;
            try {
                var realConnection = DriverManager.getConnection(config.getJdbcUrl(),
                        config.getUsername(), config.getPassword());
                connection = new ProxyConnection(realConnection, connectionPool);
            } catch (SQLException e) {
                throw new BibernateDataSourceException("Can't create connection to DB", e);
            }
            connectionPool.add(connection);
        }
        log.debug("Connection Pool size: {}", connectionPool.size());
        log.debug("Start completed Bibernate Datasource ...");
    }

    /**
     * Closes all connections in the connection pool.
     *
     * @throws ConnectionPoolException if an error occurs while closing connections
     */
    public void close() {
        while (!connectionPool.isEmpty()) {
            var connection = (ProxyConnection) connectionPool.poll();
            try {
                connection.release();
                log.trace("Closing database connection ...");
            } catch (SQLException e) {
                throw new ConnectionPoolException("Can't close connection from Connection Pool", e);
            }
        }
    }

    /**
     * Retrieves a Connection wrapped by ProxyConnection from the connection pool.
     *
     * @return a Connection object from the connection pool
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Connection getConnection() throws SQLException {
        return connectionPool.poll();
    }

    // DataSource interface methods with UnsupportedOperationException
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException(METHOD_IS_NOT_SUPPORTED);
    }

    @Override
    public PrintWriter getLogWriter() {
        throw new UnsupportedOperationException(METHOD_IS_NOT_SUPPORTED);
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        throw new UnsupportedOperationException(METHOD_IS_NOT_SUPPORTED);
    }

    @Override
    public void setLoginTimeout(int seconds) {
        throw new UnsupportedOperationException(METHOD_IS_NOT_SUPPORTED);
    }

    @Override
    public int getLoginTimeout() {
        throw new UnsupportedOperationException(METHOD_IS_NOT_SUPPORTED);
    }

    @Override
    public Logger getParentLogger() {
        throw new UnsupportedOperationException(METHOD_IS_NOT_SUPPORTED);
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        throw new UnsupportedOperationException(METHOD_IS_NOT_SUPPORTED);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        throw new UnsupportedOperationException(METHOD_IS_NOT_SUPPORTED);
    }
}
