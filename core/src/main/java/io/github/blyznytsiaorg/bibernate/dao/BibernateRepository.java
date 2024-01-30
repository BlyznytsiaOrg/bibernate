package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.annotation.Param;

import java.util.List;
import java.util.Optional;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public interface BibernateRepository<T, ID> {

    Optional<T> findById(@Param("id") ID primaryKey);

    T findOne(@Param("id") ID primaryKey);

    List<T> findAll();

    int update(T entity);

    T save(T entity);

    void saveAll(List<T> entities);

    void delete(@Param("id") ID primaryKey);

    void deleteAll(@Param("ids") List<ID> ids);
}
