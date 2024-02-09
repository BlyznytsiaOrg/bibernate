package io.github.blyznytsiaorg.bibernate.session;

import io.github.blyznytsiaorg.bibernate.cache.DistributedSet;
import io.github.blyznytsiaorg.bibernate.dao.Dao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class BibernateSecondLevelCacheSession implements BibernateSession {

    public static final String DOT = ".";
    private static final String SEPARATOR = "_";

    private final BibernateSession bibernateSession;
    private final DistributedSet distributedSet;

    @Override
    public <T> Optional<T> findById(Class<T> entityClass, Object primaryKey) {
        var cacheKey = entityClass.getPackageName() + DOT + entityClass.getSimpleName() + SEPARATOR + primaryKey;

        Optional<T> cachedEntity = distributedSet.get(entityClass, cacheKey);

        if (cachedEntity.isPresent()) {
            return cachedEntity;
        }

        Optional<T> entityFromDb = bibernateSession.findById(entityClass, primaryKey);
        entityFromDb.ifPresent(t -> distributedSet.add(entityClass, cacheKey, t));

        return entityFromDb;
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
        bibernateSession.update(entityClass, entity);
    }

    @Override
    public int find(String query, Object[] bindValues) {
        return bibernateSession.find(query, bindValues);
    }

    @Override
    public <T> T save(Class<T> entityClass, Object entity) {
        return bibernateSession.save(entityClass, entity);
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
    public <T> List<T> deleteByColumnValue(Class<T> entityClass, String columnName, Object columnValue) {
        return bibernateSession.deleteByColumnValue(entityClass, columnName, columnValue);
    }

    @Override
    public <T> void delete(Class<T> entityClass, Object entity) {
        bibernateSession.delete(entityClass, entity);
    }

    @Override
    public void close() {
        bibernateSession.close();
    }

    @Override
    public Dao getDao() {
        return bibernateSession.getDao();
    }
}
