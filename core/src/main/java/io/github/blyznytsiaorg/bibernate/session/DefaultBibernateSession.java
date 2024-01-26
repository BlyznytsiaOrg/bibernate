package io.github.blyznytsiaorg.bibernate.session;

import io.github.blyznytsiaorg.bibernate.dao.Dao;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultBibernateSession implements BibernateSession {

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
    public <T> List<T> findBy(Class<T> entityClass, String whereQuery, Object[] bindValues) {
        verifySessionNotClosed();
        return dao.findBy(entityClass, whereQuery, bindValues);
    }

    @Override
    public int find(String query, Object[] bindValues) {
        verifySessionNotClosed();
        return dao.find(query, bindValues);
    }

    @Override
    public <T> T save(Class<T> entityClass, Object entity) {
        verifySessionNotClosed();
        return dao.save(entityClass, entity);
    }

    @Override
    public <T> void delete(Class<T> entityClass, Object primaryKey) {
        verifySessionNotClosed();
        dao.delete(entityClass, primaryKey);
    }

    @Override
    public void close() {
        closed = true;
        log.info("Close session...");
    }

    @Override
    public Dao getDao() {
        verifySessionNotClosed();
        return dao;
    }

    private void verifySessionNotClosed() {
        if (closed) {
            throw new BibernateGeneralException("Session is closed");
        }
    }
}
