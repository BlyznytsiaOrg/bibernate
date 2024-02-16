package io.github.blyznytsiaorg.bibernate.transaction;

import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_CLOSE_CONNECTION;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_CLOSE_PS;

/**
 * The {@code TransactionJdbcUtils} class provides utility methods for handling JDBC resources in the context of transactions.
 * It encapsulates methods to close JDBC connections and prepared statements, considering the transactional context.
 * <p>
 *     The class includes methods to close both {@code PreparedStatement} and {@code Connection} objects,
 *     ensuring proper handling of resources and exceptions.
 * </p>
 * <p>
 *     This utility class is designed to simplify the management of JDBC resources, ensuring proper handling of exceptions
 *     and avoiding common pitfalls related to connection management in transactional contexts.
 * </p>
 *
 * @see TransactionHolder
 * @see PreparedStatement
 * @see Connection
 * @see BibernateGeneralException
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class TransactionJdbcUtils {

    /**
     * Closes both the provided {@code Connection} and {@code PreparedStatement} objects.
     * This method ensures proper resource handling, and any exceptions during the process are encapsulated
     * in a {@code BibernateGeneralException}.
     *
     * @param connection The JDBC connection to close
     * @param ps The prepared statement to close
     * @throws BibernateGeneralException If an error occurs while closing the resources
     */
    public static void close(Connection connection, PreparedStatement ps) {
        try {
            close(ps);
        } finally {
            close(connection);
        }
    }

    /**
     * Closes the provided {@code PreparedStatement} object.
     * This method ensures proper resource handling, and any exceptions during the process are encapsulated
     * in a {@code BibernateGeneralException}.
     *
     * @param ps The prepared statement to close
     * @throws BibernateGeneralException If an error occurs while closing the prepared statement
     */

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

    /**
     * Closes the provided {@code Connection} object if there is no active transaction.
     * This method checks if there is an active transaction using the {@code TransactionHolder},
     * and only closes the connection if there is no active transaction.
     * Any exceptions during the process are encapsulated in a {@code BibernateGeneralException}.
     *
     * @param connection The JDBC connection to close
     * @throws BibernateGeneralException If an error occurs while closing the connection
     */
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
