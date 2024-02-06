package io.github.blyznytsiaorg.bibernate.cache;


import java.util.Optional;

public interface DistributedSet {

    <T> void add(Class<T> entityClass, String cacheKey, T entity);


    <T> Optional<T> get(Class<T> entityClass, String cacheKey);

    <T> void remove(Class<T> entityClass, String cacheKey, T entity);
}
