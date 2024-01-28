package io.github.blyznytsiaorg.bibernate.session;

import io.github.blyznytsiaorg.bibernate.dao.Dao;
import io.github.blyznytsiaorg.bibernate.exception.BibernateSessionClosedException;
import io.github.blyznytsiaorg.bibernate.exception.ImmutableEntityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
public class DefaultBibernateSession implements BibernateSession {

    public static final String IMMUTABLE_ENTITY_S_NOT_ALLOWED_TO_CHANGE = "Immutable entity %s not allowed to change";
    private final Dao dao;
    private boolean closed;

    @Override
    public <T> Optional<T> findById(Class<T> entityClass, Object primaryKey) {
        verifySessionNotClosed();
        return dao.findById(entityClass, primaryKey);
    }

    @Override
    public <T> List<T> findAllById(Class<T> entityClass, String idColumnName, Object idColumnValue) {
        verifySessionNotClosed();
        return dao.findAllById(entityClass, idColumnName, idColumnValue);
    }

    @Override
    public <T> List<T> findByWhere(Class<T> entityClass, String whereQuery, Object[] bindValues) {
        verifySessionNotClosed();
        return dao.findByWhere(entityClass, whereQuery, bindValues);
    }

    @Override
    public <T> List<T> findByQuery(Class<T> entityClass, String query, Object[] bindValues) {
        verifySessionNotClosed();
        return dao.findByQuery(entityClass, query, bindValues);
    }

    @Override
    public int find(String query, Object[] bindValues) {
        verifySessionNotClosed();
        return dao.find(query, bindValues);
    }

    @Override
    public <T> T save(Class<T> entityClass, Object entity) {
        verifySessionNotClosed();
        if (isImmutable(entityClass)) {
            throw new ImmutableEntityException(
                    IMMUTABLE_ENTITY_S_NOT_ALLOWED_TO_CHANGE.formatted(entityClass)
            );
        }
        return dao.save(entityClass, entity);
    }

    @Override
    public <T> void delete(Class<T> entityClass, Object primaryKey) {
        verifySessionNotClosed();
        if (isImmutable(entityClass)) {
            log.warn(IMMUTABLE_ENTITY_S_NOT_ALLOWED_TO_CHANGE.formatted(entityClass));
            return;
        }
        dao.delete(entityClass, primaryKey);
    }

    @Override
    public void close() {
        closed = true;
        log.trace("Close session...");
    }

    @Override
    public Dao getDao() {
        verifySessionNotClosed();
        return dao;
    }

    private void verifySessionNotClosed() {
        if (closed) {
            log.error("Session is closed: unable to perform calls to the database.");
            throw new BibernateSessionClosedException();
        }
    }
}
