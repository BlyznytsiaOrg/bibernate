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

@Slf4j
public class BibernateDataSource implements DataSource {
    public static final String METHOD_IS_NOT_SUPPORTED = "Method is not supported";
    private final Queue<Connection> connectionPool = new ConcurrentLinkedQueue<>();

    public BibernateDataSource(BibernateDatasSourceConfig config) {
        log.debug("Starting Bibernate Datasource ...");
        for (int i = 0; i < config.getMaximumPoolSize(); i++) {
            Connection connection;
            try {
                Connection realConnection = DriverManager.getConnection(config.getJdbcUrl(),
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

    public void close(){
        while (!connectionPool.isEmpty()) {
            ProxyConnection connection = (ProxyConnection)connectionPool.poll();
            try {
                connection.release();
                log.trace("Closing database connection ...");
            } catch (SQLException e) {
                throw new ConnectionPoolException("Can't close connection from Connection Pool", e);
            }
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connectionPool.poll();
    }

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
