package io.github.blyznytsiaorg.bibernate.transaction;

import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_CLOSE_CONNECTION;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_CLOSE_PS;

public class TransactionJdbcUtils {

    public static void close(Connection connection, PreparedStatement ps) {
        try {
            close(ps);
        } finally {
            close(connection);
        }
    }

    public static void close(PreparedStatement ps) {
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException e) {
            throw new BibernateGeneralException(
                    CANNOT_CLOSE_PS.formatted(ps, e.getMessage()), e);
        }
    }

    public static void close(Connection connection) {
        try {
            if (connection != null && TransactionHolder.getTransaction() == null) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new BibernateGeneralException(
                    CANNOT_CLOSE_CONNECTION.formatted(connection, e.getMessage()), e);
        }
    }
}
