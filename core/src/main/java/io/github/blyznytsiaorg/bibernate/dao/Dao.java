package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.BibernateSession;
import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;

import java.util.List;
import java.util.Optional;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public interface Dao {

    <T> Optional<T> findById(Class<T> entityClass, Object primaryKey, BibernateSession session);

    <T> T update(Class<T> entityClass, Object primaryKey, List<ColumnSnapshot> diff);
}
