package io.github.blyznytsiaorg.bibernate.session;

import io.github.blyznytsiaorg.bibernate.dao.Dao;
import io.github.blyznytsiaorg.bibernate.exception.BibernateSessionClosedException;
import io.github.blyznytsiaorg.bibernate.exception.ImmutableEntityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.isImmutable;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class CloseBibernateSession implements BibernateSession {

    private static final String IMMUTABLE_ENTITY_S_NOT_ALLOWED_TO_CHANGE =
            "Immutable entity %s not allowed to change";
    private static final String SESSION_IS_CLOSED_UNABLE_TO_PERFORM_CALLS_TO_THE_DATABASE =
            "Session is closed: unable to perform calls to the database.";
    private final BibernateSession bibernateSession;
    private boolean closed;

    @Override
    public <T> Optional<T> findById(Class<T> entityClass, Object primaryKey) {
        verifySessionNotClosed();
        return bibernateSession.findById(entityClass, primaryKey);
    }

    @Override
    public <T> List<T> findAll(Class<T> entityClass) {
        return bibernateSession.findAll(entityClass);
    }

    @Override
    public <T> List<T> findAllByColumnValue(Class<T> entityClass, String columnName, Object columnValue) {
        verifySessionNotClosed();
        return bibernateSession.findAllByColumnValue(entityClass, columnName, columnValue);
    }

    @Override
    public <T> List<T> findByWhere(Class<T> entityClass, String whereQuery, Object[] bindValues) {
        verifySessionNotClosed();
        return bibernateSession.findByWhere(entityClass, whereQuery, bindValues);
    }

    @Override
    public <T> List<T> findByJoinTableField(Class<T> entityClass, Field field, Object... bindValues) {
        verifySessionNotClosed();
        return bibernateSession.findByJoinTableField(entityClass, field, bindValues);
    }

    @Override
    public <T> List<T> findByQuery(Class<T> entityClass, String query, Object[] bindValues) {
        verifySessionNotClosed();
        return bibernateSession.findByQuery(entityClass, query, bindValues);
    }

    @Override
    public <T> void update(Class<T> entityClass, Object entity) {
        verifySessionNotClosed();
        bibernateSession.update(entityClass, entity);
    }

    @Override
    public int find(String query, Object[] bindValues) {
        verifySessionNotClosed();
        return bibernateSession.find(query, bindValues);
    }

    @Override
    public <T> T save(Class<T> entityClass, T entity) {
        verifySessionNotClosed();
        if (isImmutable(entityClass)) {
            throw new ImmutableEntityException(
                    IMMUTABLE_ENTITY_S_NOT_ALLOWED_TO_CHANGE.formatted(entityClass)
            );
        }
        return bibernateSession.save(entityClass, entity);
    }

    @Override
    public <T> void saveAll(Class<T> entityClass, Collection<T> entity) {
        bibernateSession.saveAll(entityClass, entity);
    }

    @Override
    public <T> void deleteById(Class<T> entityClass, Object primaryKey) {
        verifySessionNotClosed();
        if (isImmutable(entityClass)) {
            log.warn(IMMUTABLE_ENTITY_S_NOT_ALLOWED_TO_CHANGE.formatted(entityClass));
            return;
        }
        bibernateSession.deleteById(entityClass, primaryKey);
    }

    @Override
    public <T> void deleteAllById(Class<T> entityClass, Collection<Object> primaryKeys) {
        verifySessionNotClosed();
        if (isImmutable(entityClass)) {
            log.warn(IMMUTABLE_ENTITY_S_NOT_ALLOWED_TO_CHANGE.formatted(entityClass));
            return;
        }
        bibernateSession.deleteAllById(entityClass, primaryKeys);
    }

    @Override
    public <T> List<T> deleteByColumnValue(Class<T> entityClass, String columnName, Object columnValue) {
        verifySessionNotClosed();
        if (isImmutable(entityClass)) {
            log.warn(IMMUTABLE_ENTITY_S_NOT_ALLOWED_TO_CHANGE.formatted(entityClass));
            return Collections.emptyList();
        }
        return bibernateSession.deleteByColumnValue(entityClass, columnName, columnValue);
    }

    @Override
    public <T> void delete(Class<T> entityClass, Object entity) {
        verifySessionNotClosed();
        if (isImmutable(entityClass)) {
            log.warn(IMMUTABLE_ENTITY_S_NOT_ALLOWED_TO_CHANGE.formatted(entityClass));
            return;
        }
        bibernateSession.delete(entityClass, entity);
    }

    @Override
    public <T> void deleteAll(Class<T> entityClass, Collection<T> entities) {
        verifySessionNotClosed();
        if (isImmutable(entityClass)) {
            log.warn(IMMUTABLE_ENTITY_S_NOT_ALLOWED_TO_CHANGE.formatted(entityClass));
            return;
        }
        bibernateSession.deleteAll(entityClass, entities);
    }

    @Override
    public void flush() {
        bibernateSession.flush();
    }

    @Override
    public void close() {
        closed = true;
        bibernateSession.close();
    }

    @Override
    public Dao getDao() {
        verifySessionNotClosed();
        return bibernateSession.getDao();
    }

    private void verifySessionNotClosed() {
        if (closed) {
            log.error(SESSION_IS_CLOSED_UNABLE_TO_PERFORM_CALLS_TO_THE_DATABASE);
            throw new BibernateSessionClosedException(SESSION_IS_CLOSED_UNABLE_TO_PERFORM_CALLS_TO_THE_DATABASE);
        }
    }
}
