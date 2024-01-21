package io.github.blyznytsiaorg.bibernate;

import io.github.blyznytsiaorg.bibernate.dao.Dao;

import java.io.Closeable;
import java.util.Optional;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public interface BibernateSession extends Closeable {
    <T> Optional<T> findById(Class<T> clazz, Object primaryKey);

    default void flush() {

    }

    @Override
    void close();

    Dao getDao();
}
