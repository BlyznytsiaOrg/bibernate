package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Interface representing a Data Access Object (DAO) for performing CRUD operations and querying on entities.
 * Defines methods to interact with the underlying data store, such as fetching entities by ID, querying based on conditions,
 * updating, saving, and deleting entities, and managing transactions.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public interface Dao {

    /**
     * Finds an entity by its primary key.
     *
     * @param entityClass The class of the entity.
     * @param primaryKey  The primary key of the entity to be retrieved.
     * @param <T>         The generic type representing the entity class.
     * @return An optional containing the entity, or empty if not found.
     */
    <T> Optional<T> findById(Class<T> entityClass, Object primaryKey);

    /**
     * Retrieves all entities of a given type.
     *
     * @param entityClass The class of the entity.
     * @param <T>         The generic type representing the entity class.
     * @return A list of all entities of the specified type.
     */
    <T> List<T> findAll(Class<T> entityClass);

    /**
     * Retrieves entities by their primary keys.
     *
     * @param entityClass The class of the entity.
     * @param primaryKeys Collection of primary keys for entities to be retrieved.
     * @param <T>         The generic type representing the entity class.
     * @return A list of entities matching the provided primary keys.
     */
    <T> List<T> findAllById(Class<T> entityClass, Collection<Object> primaryKeys);

    /**
     * Retrieves entities based on the value of a specific column.
     *
     * @param entityClass The class of the entity.
     * @param columnName  The name of the column.
     * @param columnValue The value of the column.
     * @param <T>         The generic type representing the entity class.
     * @return A list of entities matching the criteria.
     */
    <T> List<T> findAllByColumnValue(Class<T> entityClass, String columnName, Object columnValue);

    /**
     * Retrieves entities based on a custom WHERE condition.
     *
     * @param entityClass    The class of the entity.
     * @param whereCondition The WHERE condition.
     * @param bindValues     Values to bind to the WHERE condition.
     * @param <T>            The generic type representing the entity class.
     * @return A list of entities matching the criteria.
     */
    <T> List<T> findByWhere(Class<T> entityClass, String whereCondition, Object... bindValues);

    /**
     * Retrieves entities by joining a table using a specified field.
     *
     * @param entityClass The class of the entity.
     * @param field       The field used for the join.
     * @param bindValues  Values to bind to the join condition.
     * @param <T>         The generic type representing the entity class.
     * @return A list of entities matching the criteria.
     */
    <T> List<T> findByJoinTableField(Class<T> entityClass, Field field, Object... bindValues);

    /**
     * Retrieves a single entity by joining tables based on custom conditions.
     *
     * @param entityClass The class of the entity.
     * @param bindValues  Values to bind to the join conditions.
     * @param <T>         The generic type representing the entity class.
     * @return A list of entities matching the criteria.
     */
    <T> List<T> findOneByWhereJoin(Class<T> entityClass, Object... bindValues);

    /**
     * Retrieves entities based on a custom query.
     *
     * @param entityClass The class of the entity.
     * @param query       The custom query.
     * @param bindValues  Values to bind to the query.
     * @param <T>         The generic type representing the entity class.
     * @return A list of entities matching the criteria.
     */
    <T> List<T> findByQuery(Class<T> entityClass, String query, Object... bindValues);

    /**
     * Executes a custom query and returns the number of affected rows.
     *
     * @param query      The custom query.
     * @param bindValues Values to bind to the query.
     * @return The number of affected rows.
     */
    int find(String query, Object[] bindValues);

    /**
     * Updates an entity with the provided changes.
     *
     * @param entityClass The class of the entity.
     * @param entity      The entity to be updated.
     * @param diff        List of changes to apply.
     * @param <T>         The generic type representing the entity class.
     */
    <T> void update(Class<T> entityClass, Object entity, List<ColumnSnapshot> diff);

    /**
     * Saves a new entity.
     *
     * @param entityClass The class of the entity.
     * @param entity      The entity to be saved.
     * @param <T>         The generic type representing the entity class.
     * @return The saved entity.
     */
    <T> T save(Class<T> entityClass, T entity);

    /**
     * Saves a collection of entities.
     *
     * @param entityClass The class of the entities.
     * @param entities    The entities to be saved.
     * @param <T>         The generic type representing the entity class.
     */
    <T> void saveAll(Class<T> entityClass, Collection<T> entities);

    /**
     * Deletes an entity by its primary key.
     *
     * @param entityClass The class of the entity.
     * @param primaryKey  The primary key of the entity to be deleted.
     * @param <T>         The generic type representing the entity class.
     */
    <T> void deleteById(Class<T> entityClass, Object primaryKey);

    /**
     * Deletes entities by their primary keys.
     *
     * @param entityClass The class of the entities.
     * @param primaryKeys Collection of primary keys for entities to be deleted.
     * @param <T>         The generic type representing the entity class.
     */
    <T> void deleteAllById(Class<T> entityClass, Collection<Object> primaryKeys);

    /**
     * Deletes entities based on the value of a specific column.
     *
     * @param entityClass The class of the entities.
     * @param columnName  The name of the column.
     * @param value       The value of the column.
     * @param <T>         The generic type representing the entity class.
     * @return A list of deleted entities.
     */
    <T> List<T> deleteByColumnValue(Class<T> entityClass, String columnName, Object value);

    /**
     * Deletes an entity.
     *
     * @param entityClass The class of the entity.
     * @param entity      The entity to be deleted.
     * @param <T>         The generic type representing the entity class.
     */
    <T> void delete(Class<T> entityClass, Object entity);

    /**
     * Deletes a collection of entities.
     *
     * @param entityClass The class of the entities.
     * @param entities    The entities to be deleted.
     * @param <T>         The generic type representing the entity class.
     */
    <T> void deleteAll(Class<T> entityClass, Collection<T> entities);

    /**
     * Starts a transaction.
     *
     * @throws SQLException If an SQL exception occurs.
     */
    void startTransaction() throws SQLException;

    /**
     * Commits the current transaction.
     *
     * @throws SQLException If an SQL exception occurs.
     */
    void commitTransaction() throws SQLException;

    /**
     * Rolls back the current transaction.
     *
     * @throws SQLException If an SQL exception occurs.
     */
    void rollbackTransaction() throws SQLException;
}
