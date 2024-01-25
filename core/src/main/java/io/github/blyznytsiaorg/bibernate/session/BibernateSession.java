package io.github.blyznytsiaorg.bibernate.session;

import io.github.blyznytsiaorg.bibernate.dao.Dao;

import java.io.Closeable;
import java.util.List;
import java.util.Optional;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public interface BibernateSession extends Closeable {

    <T> Optional<T> findById(Class<T> entityClass, Object primaryKey);

    <T> List<T> findBy(Class<T> entityClass, String whereQuery, Object[] bindValues);

    int find(String query, Object[] bindValues);

    <T> T save(Class<T> entityClass, Object entity);

    default void flush() {

    }

    @Override
    void close();

    Dao getDao();
}
