package io.github.blyznytsiaorg.bibernate.connectionpool;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.ShardingKey;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.Executor;

/**
 * ProxyConnection is a wrapper around a JDBC Connection that intercepts calls to create statements and provides
 * additional functionality such as managing statement lifecycles and releasing connections back to a connection pool.
 *
 * @see BibernateDataSource
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class ProxyConnection implements Connection {

    /**
     * The physical JDBC Connection being wrapped.
     */
    private final Connection connection;

    /**
     * The connection pool to which this connection belongs.
     */
    private final Queue<Connection> connectionPool;

    /**
     * List of statements associated with this connection.
     */
    private final List<Statement> statementList = new ArrayList<>();

    /**
     * Constructs a ProxyConnection object with the given JDBC Connection and connection pool.
     *
     * @param connection    the JDBC Connection to wrap
     * @param connectionPool the connection pool to which this connection belongs
     */
    public ProxyConnection(Connection connection, Queue<Connection> connectionPool) {
        this.connection = connection;
        this.connectionPool = connectionPool;
    }

    /**
     * Creates a Statement object for sending SQL statements to the database.
     * Adds the created PreparedStatement to the list of statements associated with this connection.
     *
     * @return a new Statement object
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Statement createStatement() throws SQLException {
        Statement statement = connection.createStatement();
        statementList.add(statement);
        return statement;
    }

    /**
     * Creates a PreparedStatement object for sending parameterized SQL statements to the database.
     * Adds the created PreparedStatement to the list of statements associated with this connection.
     *
     * @param sql the SQL statement to be sent to the database
     * @return a new PreparedStatement object
     * @throws SQLException if a database access error occurs
     */
    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        statementList.add(statement);
        return statement;
    }

    /**
     * Creates a CallableStatement object for calling database stored procedures.
     * Adds the created CallableStatement to the list of statements associated with this connection.
     *
     * @param sql the SQL statement to be sent to the database
     * @return a new CallableStatement object
     * @throws SQLException if a database access error occurs
     */
    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        CallableStatement callableStatement = connection.prepareCall(sql);
        statementList.add(callableStatement);
        return callableStatement;
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return connection.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        connection.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return connection.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        connection.commit();
    }

    @Override
    public void rollback() throws SQLException {
        connection.rollback();
    }

    /**
     * Releases this connection back to the connection pool.
     * Also closes all associated statements and clears the statement list.
     *
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void close() throws SQLException {
        for (Statement statement : statementList) {
            statement.close();
        }
        statementList.clear();
        connectionPool.add(this);
    }

    /**
     * Releases this connection and closes all associated statements.
     * This method is typically called when the connection is no longer needed and can be closed entirely.
     * It closes all associated statements and then closes the underlying physical connection to the database.
     *
     * @throws SQLException if a database access error occurs
     */
    public void release() throws SQLException {
        for (Statement statement : statementList) {
            statement.close();
        }
        statementList.clear();
        connection.close();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return connection.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return connection.getMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        connection.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return connection.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        connection.setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
        return connection.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        connection.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return connection.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return connection.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        connection.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws SQLException {
        Statement statement = connection.createStatement(resultSetType, resultSetConcurrency);
        statementList.add(statement);
        return statement;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        statementList.add(statement);
        return statement;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        CallableStatement callableStatement = connection.prepareCall(sql, resultSetType, resultSetConcurrency);
        statementList.add(callableStatement);
        return callableStatement;
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return connection.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        connection.setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        connection.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return connection.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return connection.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return connection.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        connection.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        connection.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency,
                                     int resultSetHoldability) throws SQLException {
        Statement statement = connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        statementList.add(statement);
        return statement;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        statementList.add(statement);
        return statement;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        CallableStatement callableStatement = connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        statementList.add(callableStatement);
        return callableStatement;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
            throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql, autoGeneratedKeys);
        statementList.add(statement);
        return statement;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql, columnIndexes);
        statementList.add(statement);
        return statement;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames)
            throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql, columnNames);
        statementList.add(statement);
        return statement;
    }

    @Override
    public Clob createClob() throws SQLException {
        return connection.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return connection.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return connection.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return connection.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return connection.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        connection.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        connection.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return connection.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return connection.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return connection.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return connection.createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        connection.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return connection.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        connection.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        connection.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return connection.getNetworkTimeout();
    }

    @Override
    public void beginRequest() throws SQLException {
        connection.beginRequest();
    }

    @Override
    public void endRequest() throws SQLException {
        connection.endRequest();
    }

    @Override
    public boolean setShardingKeyIfValid(ShardingKey shardingKey, ShardingKey superShardingKey,
                                         int timeout) throws SQLException {
        return connection.setShardingKeyIfValid(shardingKey, superShardingKey, timeout);
    }

    @Override
    public boolean setShardingKeyIfValid(ShardingKey shardingKey, int timeout) throws SQLException {
        return connection.setShardingKeyIfValid(shardingKey, timeout);
    }

    @Override
    public void setShardingKey(ShardingKey shardingKey, ShardingKey superShardingKey)
            throws SQLException {
        connection.setShardingKey(shardingKey, superShardingKey);
    }

    @Override
    public void setShardingKey(ShardingKey shardingKey) throws SQLException {
        connection.setShardingKey(shardingKey);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return connection.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return connection.isWrapperFor(iface);
    }
}
