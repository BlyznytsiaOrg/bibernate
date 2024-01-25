package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;

import java.util.List;
import java.util.Optional;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public interface Dao {

    <T> Optional<T> findById(Class<T> entityClass, Object primaryKey);

    <T> List<T> findAllById(Class<T> entityClass, String idColumnName, Object idColumnValue);

    <T> List<T> findBy(Class<T> clazz, String whereCondition, Object... bindValues);

    <T> T update(Class<T> entityClass, Object primaryKey, List<ColumnSnapshot> diff);
}
