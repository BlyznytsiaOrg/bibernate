package io.github.blyznytsiaorg.bibernate.cache.impl;

import io.github.blyznytsiaorg.bibernate.cache.DistributedSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.cache.utils.SerializationUtils.deserialize;
import static io.github.blyznytsiaorg.bibernate.cache.utils.SerializationUtils.serialize;


/**
 * Implementation of the DistributedSet interface using Redis as the distributed cache.
 * Provides methods for adding entities to Redis and retrieving them.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class DistributedRedisSet implements DistributedSet {

    private final Jedis jedis;

    /**
     * Adds an entity to the Redis cache.
     *
     * @param <T>         the type of the entity
     * @param entityClass the class of the entity
     * @param cacheKey    the cache key associated with the entity
     * @param entity      the entity to add to the cache
     * @throws NullPointerException if cacheKey or entity is null
     */
    @Override
    public <T> void add(Class<T> entityClass, String cacheKey, T entity) {
        Objects.requireNonNull(cacheKey);
        Objects.requireNonNull(entity);

        byte[] keyData = cacheKey.getBytes(StandardCharsets.UTF_8);
        byte[] entityData = serialize(entityClass, entity);
        jedis.set(keyData, entityData);

        log.trace("Add to cache for entityClass {} by cacheKey {}", entityClass.getSimpleName(), cacheKey);
    }

    /**
     * Retrieves an entity from the Redis cache.
     *
     * @param <T>         the type of the entity
     * @param entityClass the class of the entity
     * @param cacheKey    the cache key associated with the entity
     * @return an Optional containing the retrieved entity, or empty if not found
     * @throws NullPointerException if entityClass or cacheKey is null
     */
    @Override
    public <T> Optional<T> get(Class<T> entityClass, String cacheKey) {
        Objects.requireNonNull(entityClass);
        Objects.requireNonNull(cacheKey);

        byte[] keyData = cacheKey.getBytes(StandardCharsets.UTF_8);
        byte[] data = jedis.get(keyData);

        if (Objects.isNull(data)) {
            log.trace("Cache missed for entityClass {} by cacheKey {}", entityClass.getSimpleName(), cacheKey);
            return Optional.empty();
        }

        log.trace("Cache hit for entityClass {} by cacheKey {}", entityClass.getSimpleName(), cacheKey);
        return deserialize(entityClass, data)
                .map(entityClass::cast);
    }
}
