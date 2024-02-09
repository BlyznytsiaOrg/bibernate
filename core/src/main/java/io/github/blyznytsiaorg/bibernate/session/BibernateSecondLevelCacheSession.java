package io.github.blyznytsiaorg.bibernate.session;

import io.github.blyznytsiaorg.bibernate.cache.DistributedSet;
import io.github.blyznytsiaorg.bibernate.dao.Dao;

import java.sql.SQLException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.isImmutable;

/**
 * Implementation of the {@link BibernateSession} interface that provides second-level caching functionality
 * for immutable entities fetched from the database.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class BibernateSecondLevelCacheSession implements BibernateSession {

    /**
     * Separator used in constructing cache keys.
     */
    private static final String DOT = ".";
    private static final String SEPARATOR = "_";

    /**
     * The underlying BibernateSession implementation.
     */
    private final BibernateSession bibernateSession;
    /**
     * The distributed set used for caching entities.
     */
    private final DistributedSet distributedSet;

    /**
     * Retrieves an entity by its primary key, optionally caching it if it's immutable.
     *
     * @param entityClass The class of the entity
     * @param primaryKey  The primary key of the entity
     * @return An Optional containing the entity, if found
     */
    @Override
    public <T> Optional<T> findById(Class<T> entityClass, Object primaryKey) {
        if (isImmutable(entityClass)) {
            var cacheKey = entityClass.getPackageName() + DOT + entityClass.getSimpleName() + SEPARATOR + primaryKey;

            Optional<T> cachedEntity = distributedSet.get(entityClass, cacheKey);

            if (cachedEntity.isPresent()) {
                return cachedEntity;
            }

            Optional<T> entityFromDb = bibernateSession.findById(entityClass, primaryKey);
            entityFromDb.ifPresent(t -> distributedSet.add(entityClass, cacheKey, t));
            return entityFromDb;
        }

        return bibernateSession.findById(entityClass, primaryKey);
    }


    /**
     * Retrieves all records from the specified table and maps them to a list of entities of the given type.
     *
     * @param entityClass The Class object representing the type of entities to be retrieved.
     * @param <T>         The generic type representing the entity class.
     * @return A list of entities of type T containing all records from the specified table.
     */
    @Override
    public <T> List<T> findAll(Class<T> entityClass) {
        return bibernateSession.findAll(entityClass);
    }

    /**
     * Retrieves entities by the value of a specific column.
     *
     * @param entityClass The class of the entity
     * @param columnName  The name of the column
     * @param columnValue The value of the column
     * @return A list of entities matching the criteria
     */
    @Override
    public <T> List<T> findAllByColumnValue(Class<T> entityClass, String columnName, Object columnValue) {
        return bibernateSession.findAllByColumnValue(entityClass, columnName, columnValue);
    }

    /**
     * Retrieves entities based on a custom WHERE clause and bind values.
     *
     * @param entityClass The class of the entity
     * @param whereQuery  The WHERE clause
     * @param bindValues  The bind values for parameters in the WHERE clause
     * @return A list of entities matching the criteria
     */
    @Override
    public <T> List<T> findByWhere(Class<T> entityClass, String whereQuery, Object[] bindValues) {
        return bibernateSession.findByWhere(entityClass, whereQuery, bindValues);
    }

    /**
     * Retrieves entities based on a join table field and bind values.
     *
     * @param entityClass The class of the entity
     * @param field       The field in the join table
     * @param bindValues  The bind values for parameters in the query
     * @return A list of entities matching the criteria
     */
    @Override
    public <T> List<T> findByJoinTableField(Class<T> entityClass, Field field, Object... bindValues) {
        return bibernateSession.findByJoinTableField(entityClass, field, bindValues);
    }

    /**
     * Retrieves entities based on a custom query and bind values.
     *
     * @param entityClass The class of the entity
     * @param query       The custom query string
     * @param bindValues  The bind values for parameters in the query
     * @return A list of entities matching the criteria
     */
    @Override
    public <T> List<T> findByQuery(Class<T> entityClass, String query, Object[] bindValues) {
        return bibernateSession.findByQuery(entityClass, query, bindValues);
    }

    /**
     * Updates the given entity in the database.
     *
     * @param entityClass The class of the entity
     * @param entity      The entity to update
     */
    @Override
    public <T> void update(Class<T> entityClass, Object entity) {
        bibernateSession.update(entityClass, entity);
    }

    /**
     * Finds entities based on the provided query and bind values.
     *
     * @param query      The query to execute
     * @param bindValues The bind values for parameters in the query
     * @return The number of entities found
     */
    @Override
    public int find(String query, Object[] bindValues) {
        return bibernateSession.find(query, bindValues);
    }

    /**
     * Saves the provided entity of the specified class.
     *
     * @param entityClass The class of the entity to save
     * @param entity      The entity to save
     * @param <T>         The type of the entity
     * @return The saved entity
     */
    @Override
    public <T> T save(Class<T> entityClass, T entity) {
        return bibernateSession.save(entityClass, entity);
    }

    /**
     * Flushes changes to the underlying database.
     * This method synchronizes the state of the Hibernate session with the database.
     * Any changes that have been queued for insertion, update, or deletion are executed immediately.
     */
    @Override
    public <T> void saveAll(Class<T> entityClass, Collection<T> entity) {
        bibernateSession.saveAll(entityClass, entity);
    }

    @Override
    public void flush() {
        bibernateSession.flush();
    }

    @Override
    public <T> void deleteById(Class<T> entityClass, Object primaryKey) {
        bibernateSession.deleteById(entityClass, primaryKey);
    }

    @Override
    public <T> void deleteAllById(Class<T> entityClass, Collection<Object> primaryKeys) {
        bibernateSession.deleteAllById(entityClass, primaryKeys);
    }

    @Override
    public <T> List<T> deleteByColumnValue(Class<T> entityClass, String columnName, Object columnValue) {
        return bibernateSession.deleteByColumnValue(entityClass, columnName, columnValue);
    }

    @Override
    public <T> void delete(Class<T> entityClass, Object entity) {
        bibernateSession.delete(entityClass, entity);
    }

    /**
     * Closes the session and releases any resources associated with it.
     */
    @Override
    public <T> void deleteAll(Class<T> entityClass, Collection<T> entities) {
        bibernateSession.deleteAll(entityClass, entities);
    }

    @Override
    public void close() {
        bibernateSession.close();
    }

    /**
     * Retrieves the DAO associated with this session.
     *
     * @return The DAO associated with this session
     */
    @Override
    public Dao getDao() {
        return bibernateSession.getDao();
    }

    @Override
    public void startTransaction() throws SQLException {
        bibernateSession.startTransaction();
    }

    @Override
    public void commitTransaction() throws SQLException {
        bibernateSession.commitTransaction();
    }

    @Override
    public void rollbackTransaction() throws SQLException {
        bibernateSession.rollbackTransaction();
    }
}
