package io.github.blyznytsiaorg.bibernate.session;

import io.github.blyznytsiaorg.bibernate.dao.Dao;
import io.github.blyznytsiaorg.bibernate.exception.BibernateSessionClosedException;
import io.github.blyznytsiaorg.bibernate.exception.ImmutableEntityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.ENTITY_CLASS_MUST_BE_NOT_NULL;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.ENTITY_MUST_BE_NOT_NULL;

/**
 * Wrapper class that provides validation functionality for a Bibernate session.
 * It delegates method calls to an underlying Bibernate session while performing validation checks.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class ValidatorBibernateSession implements BibernateSession {

    private final BibernateSession bibernateSession;
    @Override
    public <T> Optional<T> findById(Class<T> entityClass, Object primaryKey) {
        return bibernateSession.findById(entityClass, primaryKey);
    }

    @Override
    public <T> List<T> findAll(Class<T> entityClass) {
        return bibernateSession.findAll(entityClass);
    }

    @Override
    public <T> List<T> findAllById(Class<T> entityClass, Collection<Object> primaryKeys) {
        return bibernateSession.findAllById(entityClass, primaryKeys);
    }

    @Override
    public <T> List<T> findAllByColumnValue(Class<T> entityClass, String columnName, Object columnValue) {
        return bibernateSession.findAllByColumnValue(entityClass, columnName, columnValue);
    }

    @Override
    public <T> List<T> findByWhere(Class<T> entityClass, String whereQuery, Object[] bindValues) {
        return bibernateSession.findByWhere(entityClass, whereQuery, bindValues);
    }

    @Override
    public <T> List<T> findByJoinTableField(Class<T> entityClass, Field field, Object... bindValues) {
        return bibernateSession.findByJoinTableField(entityClass, field, bindValues);
    }

    @Override
    public <T> Optional<T> findByWhereJoin(Class<T> entityClass, Object[] bindValues) {
        return Optional.empty();
    }

    @Override
    public <T> List<T> findByQuery(Class<T> entityClass, String query, Object[] bindValues) {
        return bibernateSession.findByQuery(entityClass, query, bindValues);
    }

    /**
     * Updates the entity of the specified class in the database.
     * This method ensures that both the entity class and the entity instance are not null before proceeding with the update operation.
     * Additionally, it verifies that the entity has a strategy generator for its ID or that the ID value is not null.
     *
     * @param entityClass the class of the entity to be updated
     * @param entity      the entity instance to be updated
     * @throws NullPointerException if either {@code entityClass} or {@code entity} is {@code null}
     * @throws IllegalArgumentException if the entity does not have a strategy generator for its ID and the ID value is null
     */
    @Override
    public <T> void update(Class<T> entityClass, Object entity) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL);
        verifyIsIdHasStrategyGeneratorOrNotNullValue(entity);
        bibernateSession.update(entityClass, entity);
    }

    @Override
    public int find(String query, Object[] bindValues) {
        return bibernateSession.find(query, bindValues);
    }

    /**
     * Saves the given entity to the database.
     *
     * <p>This method ensures that both the entity class and the entity instance are not null before proceeding with the save operation.
     * Additionally, it verifies that the entity has a strategy generator for its ID or that the ID value is not null.
     *
     * @param <T>         the type of the entity
     * @param entityClass the class of the entity to be saved
     * @param entity      the entity instance to be saved
     * @return the saved entity
     * @throws NullPointerException     if either {@code entityClass} or {@code entity} is {@code null}
     * @throws IllegalArgumentException if the entity does not have a strategy generator for its ID and the ID value is null
     */
    @Override
    public <T> T save(Class<T> entityClass, T entity) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL);
        verifyIsIdHasStrategyGeneratorOrNotNullValue(entity);
        return bibernateSession.save(entityClass, entity);
    }

    /**
     * Saves all entities in the provided collection to the database.
     *
     * <p>This method ensures that the entity class is not null before proceeding with the save operation.
     * Additionally, it verifies that each entity in the collection has a strategy generator for its ID or that the ID value is not null.
     *
     * @param <T>         the type of the entities
     * @param entityClass the class of the entities to be saved
     * @param entities    the collection of entities to be saved
     * @throws NullPointerException     if {@code entityClass} is {@code null}
     * @throws IllegalArgumentException if any entity in the collection does not have a strategy generator for its ID and the ID value is null
     */
    @Override
    public <T> void saveAll(Class<T> entityClass, Collection<T> entities) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        verifyIsIdHasStrategyGeneratorOrNotNullValue(entities);
        bibernateSession.saveAll(entityClass, entities);
    }

    @Override
    public <T> void deleteById(Class<T> entityClass, Object primaryKey) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(primaryKey, ENTITY_MUST_BE_NOT_NULL);
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
    public <T> void delete(Class<T> entityClass, T entity) {
        bibernateSession.delete(entityClass, entity);
    }

    @Override
    public <T> void deleteAll(Class<T> entityClass, Collection<T> entities) {
        bibernateSession.deleteAll(entityClass, entities);
    }

    @Override
    public void flush() {
        bibernateSession.flush();
    }

    @Override
    public void close() {
        bibernateSession.close();
    }

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
