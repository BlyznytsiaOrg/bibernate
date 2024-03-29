package io.github.blyznytsiaorg.bibernate.session;

import io.github.blyznytsiaorg.bibernate.dao.Dao;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Interface representing a session with a Bibernate-based data store.
 * Provides methods for common CRUD (Create, Read, Update, Delete) operations.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public interface BibernateSession extends Closeable {

    /**
     * Finds an entity by its primary key.
     *
     * @param <T>         the type of the entity
     * @param entityClass the class of the entity
     * @param primaryKey  the primary key of the entity
     * @return an Optional containing the found entity, or empty if not found
     */
    <T> Optional<T> findById(Class<T> entityClass, Object primaryKey);

    /**
     * Retrieves all entities of a specified type from the data store.
     *
     * @param entityClass The class of the entity.
     * @param <T>         The generic type representing the entity class.
     * @return A list of all entities of the specified type in the data store.
     */
    <T> List<T> findAll(Class<T> entityClass);

    /**
     * Retrieves entities based on a collection of primary keys.
     *
     * @param entityClass The class of the entity.
     * @param primaryKeys A collection of primary keys for identifying and retrieving specific records.
     * @param <T>         The generic type representing the entity class.
     * @return A list of entities matching the provided collection of primary keys.
     */
    <T> List<T> findAllById(Class<T> entityClass, Collection<Object> primaryKeys);

    /**
     * Finds all entities of a given class with a specified column value.
     *
     * @param <T>         the type of the entity
     * @param entityClass the class of the entity
     * @param columnName  the name of the column to match
     * @param columnValue the value to match
     * @return a list of entities matching the criteria
     */
    <T> List<T> findAllByColumnValue(Class<T> entityClass, String columnName, Object columnValue);

    /**
     * Finds entities of a given class based on a custom WHERE clause.
     *
     * @param <T>         the type of the entity
     * @param entityClass the class of the entity
     * @param whereQuery  the WHERE clause to apply
     * @param bindValues  values to bind to the query parameters
     * @return a list of entities matching the criteria
     */
    <T> List<T> findByWhere(Class<T> entityClass, String whereQuery, Object[] bindValues);

    /**
     * Finds entities of a given class based on a join table field.
     *
     * @param <T>         the type of the entity
     * @param entityClass the class of the entity
     * @param field       the field to join on
     * @param bindValues  values to bind to the query parameters
     * @return a list of entities matching the criteria
     */
    <T> List<T> findByJoinTableField(Class<T> entityClass, Field field, Object... bindValues);

    <T> Optional<T> findByWhereJoin(Class<T> entityClass, Object[] bindValues);


    /**
     * Finds entities of a given class based on a custom query.
     *
     * @param <T>         the type of the entity
     * @param entityClass the class of the entity
     * @param query       the custom query to execute
     * @param bindValues  values to bind to the query parameters
     * @return a list of entities matching the criteria
     */
    <T> List<T> findByQuery(Class<T> entityClass, String query, Object[] bindValues);

    /**
     * Updates an entity in the data store.
     *
     * @param <T>         the type of the entity
     * @param entityClass the class of the entity
     * @param entity      the entity to update
     */
    <T> void update(Class<T> entityClass, Object entity);

    /**
     * Finds the number of records returned by a custom query.
     *
     * @param query      the custom query to execute
     * @param bindValues values to bind to the query parameters
     * @return the number of records returned by the query
     */
    int find(String query, Object[] bindValues);

    /**
     * Saves an entity to the data store.
     *
     * @param <T>         the type of the entity
     * @param entityClass the class of the entity
     * @param entity      the entity to save
     * @return the saved entity
     */
    <T> T save(Class<T> entityClass, T entity);

    /**
     * Saves a collection of entities into the specified table.
     *
     * @param entityClass The Class object representing the type of entities to be saved.
     * @param entities    A collection of entities to be persisted into the database.
     * @param <T>         The generic type representing the entity class.
     */
    <T> void saveAll(Class<T> entityClass, Collection<T> entities);

    /**
     * Flushes changes to the underlying database.
     * This method synchronizes the state of the Bibernate session with the database.
     * Any changes that have been queued for insertion, update, or deletion are executed immediately.
     */
    default void flush() {

    }

    /**
     * Deletes an entity from the data store by its primary key.
     *
     * @param <T>         the type of the entity
     * @param entityClass the class of the entity
     * @param primaryKey  the primary key of the entity to delete
     */
    <T> void deleteById(Class<T> entityClass, Object primaryKey);

    /**
     * Deletes records from the specified table based on the provided collection of primary key values.
     *
     * @param entityClass The Class object representing the type of entities for which records will be deleted.
     * @param primaryKeys A collection of primary key values identifying the records to be deleted.
     * @param <T>         The generic type representing the entity class.
     */
    <T> void deleteAllById(Class<T> entityClass, Collection<Object> primaryKeys);

    /**
     * Deletes records from the specified table where the value in the specified column matches the given criteria.
     *
     * @param entityClass The Class object representing the type of entities for which records will be deleted.
     * @param columnName  The name of the column used in the WHERE condition for deletion.
     * @param columnValue The value to match in the specified column for deletion.
     * @param <T>         The generic type representing the entity class.
     * @return A list of entities of type T that were deleted from the table.
     */
    <T> List<T> deleteByColumnValue(Class<T> entityClass, String columnName, Object columnValue);

    /**
     * Deletes an entity from the data store.
     *
     * @param <T>         the type of the entity
     * @param entityClass the class of the entity
     * @param entity      the entity to delete
     */
    <T> void delete(Class<T> entityClass, T entity);

    /**
     * Deletes all records associated with the provided entities from the specified table.
     *
     * @param entityClass The Class object representing the type of entities to be deleted.
     * @param entities    A collection of entities whose corresponding records will be deleted.
     * @param <T>         The generic type representing the entity class.
     */
    <T> void deleteAll(Class<T> entityClass, Collection<T> entities);

    /**
     * Closes the session, releasing any resources associated with it.
     */
    @Override
    void close();

    /**
     * Gets the Data Access Object (DAO) associated with this session.
     *
     * @return the DAO instance
     */
    Dao getDao();

    /**
     * Starts a new transaction. This method should be called before any operations that
     * are meant to be part of a transaction.
     *
     * @throws SQLException if a database access error occurs or this method is called
     *         on a closed connection
     */
    void startTransaction() throws SQLException;

    /**
     * Commits the current transaction. This method should be called to make all changes
     * made within the transaction permanent.
     *
     * @throws SQLException if a database access error occurs, the connection is closed
     *         or this method is called when no transaction is active
     */
    void commitTransaction() throws SQLException;

    /**
     * Rolls back the current transaction. This method should be called if any errors
     * occur within the transaction and the changes made need to be discarded.
     *
     * @throws SQLException if a database access error occurs, the connection is closed
     *         or this method is called when no transaction is active
     */
    void rollbackTransaction() throws SQLException;
}
