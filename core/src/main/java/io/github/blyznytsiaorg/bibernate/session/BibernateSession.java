package io.github.blyznytsiaorg.bibernate.session;

import io.github.blyznytsiaorg.bibernate.dao.Dao;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public interface BibernateSession extends Closeable {

    <T> Optional<T> findById(Class<T> entityClass, Object primaryKey);

    <T> List<T> findAllByColumnValue(Class<T> entityClass, String columnName, Object columnValue);

    <T> List<T> findByWhere(Class<T> entityClass, String whereQuery, Object[] bindValues);

    <T> List<T> findByJoinTableField(Class<T> entityClass, Field field, Object... bindValues);
    
    <T> List<T> findByQuery(Class<T> entityClass, String query, Object[] bindValues);

    <T> int update(Class<T> entityClass, Object entity);

    int find(String query, Object[] bindValues);

    <T> T save(Class<T> entityClass, Object entity);

    default void flush() {

    }

    <T> void deleteById(Class<T> entityClass, Object primaryKey);

    <T> void delete(Class<T> entityClass, Object entity);

    @Override
    void close();

    Dao getDao();
}
