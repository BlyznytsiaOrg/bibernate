package io.github.blyznytsiaorg.bibernate.cache;

import java.util.Optional;

/**
 * Interface representing a distributed set for caching entities.
 * Provides methods for adding entities to the set and retrieving them.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public interface DistributedSet {
    /**
     * Adds an entity to the distributed set.
     *
     * @param <T>         the type of the entity
     * @param entityClass the class of the entity
     * @param cacheKey    the cache key associated with the entity
     * @param entity      the entity to add to the set
     */
    <T> void add(Class<T> entityClass, String cacheKey, T entity);

    /**
     * Retrieves an entity from the distributed set.
     *
     * @param <T>         the type of the entity
     * @param entityClass the class of the entity
     * @param cacheKey    the cache key associated with the entity
     * @return an Optional containing the retrieved entity, or empty if not found
     */
    <T> Optional<T> get(Class<T> entityClass, String cacheKey);
}
