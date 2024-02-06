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

@RequiredArgsConstructor
@Slf4j
public class DistributedRedisSet implements DistributedSet {

    private final Jedis jedis;

    @Override
    public <T> void add(Class<T> entityClass, String cacheKey, T entity) {
        Objects.requireNonNull(cacheKey);
        Objects.requireNonNull(entity);

        byte[] keyData = cacheKey.getBytes(StandardCharsets.UTF_8);
        byte[] entityData = serialize(entityClass, entity);
        jedis.set(keyData, entityData);

        //TODO need to think about some limit pear entityClass

        log.trace("Add to cache for entityClass {} by cacheKey {}", entityClass.getSimpleName(), cacheKey);
    }

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

    @Override
    public <T> void remove(Class<T> entityClass, String cacheKey, T entity) {
        Objects.requireNonNull(entityClass);
        Objects.requireNonNull(cacheKey);

        byte[] keyData = cacheKey.getBytes(StandardCharsets.UTF_8);
        byte[] entityData = serialize(entityClass, entity);
        jedis.srem(keyData, entityData);
    }
}
