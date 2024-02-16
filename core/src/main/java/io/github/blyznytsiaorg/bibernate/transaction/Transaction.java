package io.github.blyznytsiaorg.bibernate.transaction;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.setIdField;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enables the application to delineate units of work while abstracting away the specifics of the underlying
 * transaction implementation.
 * A representation of a database transaction that manages a connection, tracks updated entities, and supports
 * transactional operations such as starting, committing, and rolling back.
 * <p>
 * Instances of this class are typically used to group a set of database operations into a single atomic unit.</p>
 * A transaction is linked with a BibernateSession and is typically initiated through a call to bibernateSession.startTransaction().
 * The design anticipates having, at most, one uncommitted Transaction associated with a specific BibernateSession concurrently.
 *
 * @see Connection
 * @see SQLException
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@RequiredArgsConstructor
@Getter
public class Transaction {

    private final Connection connection;

    private final Set<Object> updatedEntities = new HashSet<>();

    /**
     * Starts the transaction by setting auto-commit to false on the associated database connection.
     *
     * @throws SQLException If an SQL exception occurs while starting the transaction.
     */
    public void start() throws SQLException {
        connection.setAutoCommit(false);
    }

    /**
     * Commits the transaction, and end the unit of work, applies changes to the database, and closes the associated connection.
     * Clears the set of updated entities after a successful commit.
     *
     * @throws SQLException If an SQL exception occurs while committing the transaction or closing the connection.
     */
    public void commit() throws SQLException {
        connection.commit();
        connection.close();
        updatedEntities.clear();
    }

    /**
     * Force the underlying transaction to roll back, undoing changes made during the transaction,
     * and closes the associated connection.
     * Resets the ID fields of all updated entities to null during rollback.
     *
     * @throws SQLException If an SQL exception occurs while rolling back the transaction or closing the connection.
     */
    public void rollback() throws SQLException {
        connection.rollback();
        connection.close();
        rollbackAllIds();
    }

    /**
     * Adds an entity to the set of updated entities.
     * Entities in this set are considered modified during the transaction.
     * This set is contained here in order to be able to revert changes to the entity in the rollback case
     *
     * @param entity The entity to be added to the set of updated entities.
     */
    public void addUpdatedEntity(Object entity) {
        updatedEntities.add(entity);
    }

    /**
     * Rolls back the ID fields of all entities in the updatedEntities set by setting them to null.
     * This operation is typically performed during a rollback to undo changes made during the transaction.
     */
    private void rollbackAllIds() {
        for (var entity : updatedEntities) {
            setIdField(entity, null);
        }
    }
}
