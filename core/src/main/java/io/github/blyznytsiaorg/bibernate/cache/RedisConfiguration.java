package io.github.blyznytsiaorg.bibernate.cache;

import io.github.blyznytsiaorg.bibernate.cache.impl.DistributedRedisSet;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;


/**
 * Configuration class for setting up Redis connections and distributed caching.
 * It initializes a Jedis instance based on provided database settings and creates a DistributedRedisSet instance.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Getter
@Slf4j
public class RedisConfiguration {

    private Jedis jedis;

    /**
     * Constructs a new RedisConfiguration instance and sets up the Redis connection.
     *
     * @param bibernateDatabaseSettings the database settings containing Redis configuration
     */
    public RedisConfiguration(final BibernateDatabaseSettings bibernateDatabaseSettings) {
        setup(bibernateDatabaseSettings);
    }

    /**
     * Sets up the Redis connection based on the provided database settings.
     * If second-level cache is enabled, it initializes a Jedis instance with the specified host and port.
     *
     * @param bibernateDatabaseSettings the database settings containing Redis configuration
     */
    private void setup(BibernateDatabaseSettings bibernateDatabaseSettings) {
        if (bibernateDatabaseSettings.isSecondLevelCacheEnabled()) {
            this.jedis = new Jedis(
                    bibernateDatabaseSettings.getSecondLevelCacheHost(),
                    bibernateDatabaseSettings.getSecondLevelCachePost()
            );
        }
    }

    /**
     * Retrieves a DistributedSet instance for interacting with the distributed cache.
     * Creates a new DistributedRedisSet instance with the configured Jedis client.
     *
     * @return a DistributedSet instance for interacting with the distributed cache
     */
    public DistributedSet getDistributedMap() {
        return new DistributedRedisSet(jedis);
    }
}
