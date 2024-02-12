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

    @Override
    public <T> T save(Class<T> entityClass, T entity) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL);
        verifyIsIdHasStrategyGeneratorOrNotNullValue(entity);
        return bibernateSession.save(entityClass, entity);
    }

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
