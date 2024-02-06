package io.github.blyznytsiaorg.bibernate.cache;

import io.github.blyznytsiaorg.bibernate.cache.impl.DistributedRedisSet;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

@Getter
@Slf4j
public class RedisConfiguration {

    private Jedis jedis;

    public RedisConfiguration(final BibernateDatabaseSettings bibernateDatabaseSettings) {
        setup(bibernateDatabaseSettings);
    }

    private void setup(BibernateDatabaseSettings bibernateDatabaseSettings) {
        if (bibernateDatabaseSettings.isSecondLevelCacheEnabled()) {
            this.jedis = new Jedis(
                    bibernateDatabaseSettings.getSecondLevelCacheHost(),
                    bibernateDatabaseSettings.getSecondLevelCachePost()
            );
        }
    }

    public DistributedSet getDistributedMap() {
        return new DistributedRedisSet(jedis);
    }
}
