package io.github.blyznytsiaorg.bibernate.session;

import io.github.blyznytsiaorg.bibernate.dao.Dao;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

/**
 * Interface representing a session with a Bibernate-based data store.
 * Provides methods for common CRUD (Create, Read, Update, Delete) operations.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public interface BibernateSession extends Closeable {

    /**
     * Finds an entity by its primary key.
     *
     * @param <T>          the type of the entity
     * @param entityClass  the class of the entity
     * @param primaryKey   the primary key of the entity
     * @return an Optional containing the found entity, or empty if not found
     */
    <T> Optional<T> findById(Class<T> entityClass, Object primaryKey);

    /**
     * Finds all entities of a given class with a specified column value.
     *
     * @param <T>          the type of the entity
     * @param entityClass  the class of the entity
     * @param columnName   the name of the column to match
     * @param columnValue  the value to match
     * @return a list of entities matching the criteria
     */
    <T> List<T> findAllByColumnValue(Class<T> entityClass, String columnName, Object columnValue);

    /**
     * Finds entities of a given class based on a custom WHERE clause.
     *
     * @param <T>          the type of the entity
     * @param entityClass  the class of the entity
     * @param whereQuery   the WHERE clause to apply
     * @param bindValues   values to bind to the query parameters
     * @return a list of entities matching the criteria
     */
    <T> List<T> findByWhere(Class<T> entityClass, String whereQuery, Object[] bindValues);

    /**
     * Finds entities of a given class based on a join table field.
     *
     * @param <T>          the type of the entity
     * @param entityClass  the class of the entity
     * @param field        the field to join on
     * @param bindValues   values to bind to the query parameters
     * @return a list of entities matching the criteria
     */
    <T> List<T> findByJoinTableField(Class<T> entityClass, Field field, Object... bindValues);

    <T> Optional<T> findByWhereJoin(Class<T> entityClass, Object[] bindValues);


    /**
     * Finds entities of a given class based on a custom query.
     *
     * @param <T>          the type of the entity
     * @param entityClass  the class of the entity
     * @param query        the custom query to execute
     * @param bindValues   values to bind to the query parameters
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
    <T> T save(Class<T> entityClass, Object entity);

    /**
     * Flushes changes to the underlying database.
     * This method synchronizes the state of the Hibernate session with the database.
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

    <T> List<T> deleteByColumnValue(Class<T> entityClass, String columnName, Object columnValue);

    /**
     * Deletes an entity from the data store.
     *
     * @param <T>         the type of the entity
     * @param entityClass the class of the entity
     * @param entity      the entity to delete
     */
    <T> void delete(Class<T> entityClass, Object entity);

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
}
