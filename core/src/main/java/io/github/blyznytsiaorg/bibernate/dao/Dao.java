package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.join.JoinType;
import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;
import io.github.blyznytsiaorg.bibernate.entity.EntityMetadata;

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

    <T> List<T> findByWhere(Class<T> entityClass, String whereCondition, Object... bindValues);

    List<Object> findByWhereJoin(EntityMetadata searchedEntityMetadata, Object... bindValues);

    <T> List<T> findByQuery(Class<T> entityClass, String query, Object... bindValues);

    int find(String query, Object[] bindValues);

    <T> int update(Class<T> entityClass, Object entity, List<ColumnSnapshot> diff);

    <T> T save(Class<T> entityClass, Object entity);

    <T> void deleteById(Class<T> entityClass, Object primaryKey);

    <T> void delete(Class<T> entityClass, Object entity);
}
