package io.github.blyznytsiaorg.bibernate.transaction;

import io.github.blyznytsiaorg.bibernate.connectionpool.BibernateDataSource;
import io.github.blyznytsiaorg.bibernate.connectionpool.BibernateDatasSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionalDatasource extends BibernateDataSource {

    public TransactionalDatasource(BibernateDatasSourceConfig config) {
        super(config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        var transaction = TransactionHolder.getTransaction();
        if (transaction != null) {
            return transaction.getConnection();
        }
        return super.getConnection();
    }

    @Override
    public void close() {
        TransactionHolder.removeTransaction();
        super.close();
    }
}
