package io.github.blyznytsiaorg.bibernate.transaction;

import io.github.blyznytsiaorg.bibernate.connectionpool.BibernateDataSource;
import io.github.blyznytsiaorg.bibernate.connectionpool.BibernateDatasSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@code TransactionalDatasource} extends {@code BibernateDataSource} and provides support for managing
 * transactions within the data source.
 * This class ensures that connections retrieved during an active transaction are associated with that transaction.
 * When a transaction is not active, it falls back to the default behavior of its parent class, {@code BibernateDataSource}.
 * <p>
 *     The class overrides the {@code getConnection} method to check for an active transaction using {@code TransactionHolder}.
 *     If a transaction is active, it returns the connection associated with that transaction; otherwise, it falls back
 *     to the default behavior of the parent class to retrieve a regular connection.
 * </p>
 * <p>
 *     The {@code close} method is overridden to remove the transaction association from {@code TransactionHolder}
 *     before closing the data source, ensuring proper cleanup and consistency.
 * </p>
 * <p>
 *     Instances of this class are created with a {@code BibernateDatasSourceConfig}, which is passed to its superclass,
 *     {@code BibernateDataSource}, during construction.
 * </p>
 *
 * @see BibernateDataSource
 * @see TransactionHolder
 * @see BibernateDatasSourceConfig
 * @see Connection
 * @see SQLException
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class TransactionalDatasource extends BibernateDataSource {

    /**
     * Constructs a new {@code TransactionalDatasource} with the specified configuration.
     *
     * @param config The configuration for the data source
     */
    public TransactionalDatasource(BibernateDatasSourceConfig config) {
        super(config);
    }

    /**
     * Overrides the {@code getConnection} method to provide transactional support for obtaining connections.
     * If an active transaction is present, it returns the connection associated with that transaction;
     * otherwise, it falls back to the default behavior of {@code BibernateDataSource} to retrieve a regular connection.
     *
     * @return A {@code Connection} object associated with an active transaction or a regular connection if no transaction is active
     * @throws SQLException If an SQL exception occurs while obtaining a connection
     */
    @Override
    public Connection getConnection() throws SQLException {
        var transaction = TransactionHolder.getTransaction();
        if (transaction != null) {
            return transaction.getConnection();
        }
        return super.getConnection();
    }

    /**
     * Overrides the {@code close} method to remove the transaction association from {@code TransactionHolder}
     * before closing the data source, ensuring proper cleanup and consistency.
     */
    @Override
    public void close() {
        TransactionHolder.removeTransaction();
        super.close();
    }
}
