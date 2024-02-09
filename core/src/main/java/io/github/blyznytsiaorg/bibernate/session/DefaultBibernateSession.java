package io.github.blyznytsiaorg.bibernate.session;

import io.github.blyznytsiaorg.bibernate.dao.Dao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.CLOSE_SESSION;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultBibernateSession implements BibernateSession {

    private final Dao dao;

    @Override
    public <T> Optional<T> findById(Class<T> entityClass, Object primaryKey) {
        return dao.findById(entityClass, primaryKey);
    }

    @Override
    public <T> List<T> findAll(Class<T> entityClass) {
        return dao.findAll(entityClass);
    }

    @Override
    public <T> List<T> findAllByColumnValue(Class<T> entityClass, String columnName, Object columnValue) {
        return dao.findAllByColumnValue(entityClass, columnName, columnValue);
    }

    @Override
    public <T> List<T> findByWhere(Class<T> entityClass, String whereQuery, Object[] bindValues) {
        return dao.findByWhere(entityClass, whereQuery, bindValues);
    }

    @Override
    public <T> List<T> findByJoinTableField(Class<T> entityClass, Field field, Object... bindValues) {
        return dao.findByJoinTableField(entityClass, field, bindValues);
    }

    @Override
    public <T> List<T> findByQuery(Class<T> entityClass, String query, Object[] bindValues) {
        return dao.findByQuery(entityClass, query, bindValues);
    }

    @Override
    public <T> void update(Class<T> entityClass, Object entity) {
        dao.update(entityClass, entity, List.of());
    }

    @Override
    public int find(String query, Object[] bindValues) {
        return dao.find(query, bindValues);
    }

    @Override
    public <T> T save(Class<T> entityClass, T entity) {
        return dao.save(entityClass, entity);
    }

    @Override
    public <T> void saveAll(Class<T> entityClass, Collection<T> entity) {
        dao.saveAll(entityClass, entity);
    }

    @Override
    public <T> void deleteById(Class<T> entityClass, Object primaryKey) {
        dao.deleteById(entityClass, primaryKey);
    }

    @Override
    public <T> void deleteAllById(Class<T> entityClass, Collection<Object> primaryKeys) {
        dao.deleteAllById(entityClass, primaryKeys);
    }

    @Override
    public <T> List<T> deleteByColumnValue(Class<T> entityClass, String columnName, Object columnValue) {
        return dao.deleteByColumnValue(entityClass, columnName, columnValue);
    }

    @Override
    public <T> void delete(Class<T> entityClass, Object entity) {
        dao.delete(entityClass, entity);
    }

    @Override
    public <T> void deleteAll(Class<T> entityClass, Collection<T> entities) {
        dao.deleteAll(entityClass, entities);
    }

    @Override
    public void close() {
        log.trace(CLOSE_SESSION);
    }

    @Override
    public Dao getDao() {
        return dao;
    }
}
