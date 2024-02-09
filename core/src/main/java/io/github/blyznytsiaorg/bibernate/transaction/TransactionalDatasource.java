package io.github.blyznytsiaorg.bibernate.transaction;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionalDatasource extends HikariDataSource {

    public TransactionalDatasource(HikariConfig configuration) {
        super(configuration);
    }

    @Override
    public Connection getConnection() throws SQLException {
        var transaction = TransactionHolder.getTransaction();
        if (transaction != null) {
            return transaction.getConnection();
        }
        return super.getConnection();
    }

    public void close() {
        TransactionHolder.removeTransaction();
        super.close();
    }
}
