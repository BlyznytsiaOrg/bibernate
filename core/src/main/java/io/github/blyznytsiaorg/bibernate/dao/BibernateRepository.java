package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.annotation.Param;

import java.util.Optional;

public interface BibernateRepository<ID, T> {

    Optional<T> findById(@Param("id") ID primaryKey);
}
