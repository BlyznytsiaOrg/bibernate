package io.github.blyznytsiaorg.bibernate;

import com.zaxxer.hikari.HikariDataSource;
import io.github.blyznytsiaorg.bibernate.cache.RedisConfiguration;
import io.github.blyznytsiaorg.bibernate.config.BibernateConfiguration;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.config.FlywayConfiguration;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadataCollector;
import io.github.blyznytsiaorg.bibernate.session.BibernateReflectionHolder;
import io.github.blyznytsiaorg.bibernate.entity.BibernateEntityMetadataHolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Getter
@Slf4j
public class Persistent {
    private final BibernateDatabaseSettings bibernateDatabaseSettings;

    public Persistent(String packageName) {
        var bibernateConfiguration = new BibernateConfiguration();
        var bibernateSettings = bibernateConfiguration.load();
        var configFileName = bibernateConfiguration.getConfigFileName();
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(bibernateSettings,
                configFileName);
        enableFlyway();
        BibernateReflectionHolder.setReflection(Persistent.class.getPackageName());
        bibernateDatabaseSettings.setRedisConfiguration(redisConfiguration());

        EntityMetadataCollector entityMetadataCollector = new EntityMetadataCollector(packageName);

        Map<Class<?>, EntityMetadata> inMemoryEntityMetadata = entityMetadataCollector.getInMemoryEntityMetadata();
        BibernateEntityMetadataHolder.setBibernateEntityMetadata(inMemoryEntityMetadata);
    }

    public Persistent(String configFileName, String packageName) {
        var bibernateSettings = new BibernateConfiguration(configFileName).load();
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(bibernateSettings,
                configFileName);
        enableFlyway();
        BibernateReflectionHolder.setReflection(Persistent.class.getPackageName());
        bibernateDatabaseSettings.setRedisConfiguration(redisConfiguration());
    }

    public Persistent(HikariDataSource dataSource, String packageName) {
        var bibernateConfiguration = new BibernateConfiguration();
        var bibernateSettings = bibernateConfiguration.load();
        var configFileName = bibernateConfiguration.getConfigFileName();
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(bibernateSettings,
                configFileName, dataSource);
        enableFlyway();
        BibernateReflectionHolder.setReflection(Persistent.class.getPackageName());
        bibernateDatabaseSettings.setRedisConfiguration(redisConfiguration());
    }

    public Persistent(String configFileName, HikariDataSource dataSource, String packageName) {
        var bibernateSettings = new BibernateConfiguration(configFileName).load();
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(bibernateSettings,
                configFileName, dataSource);
        enableFlyway();
        BibernateReflectionHolder.setReflection(Persistent.class.getPackageName());
        bibernateDatabaseSettings.setRedisConfiguration(redisConfiguration());
    }

    public Persistent(Map<String, String> externalBibernateSettings, String packageName) {
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(externalBibernateSettings,
                null);
        enableFlyway();
        BibernateReflectionHolder.setReflection(Persistent.class.getPackageName());
        bibernateDatabaseSettings.setRedisConfiguration(redisConfiguration());
        EntityMetadataCollector entityMetadataCollector = new EntityMetadataCollector(packageName);

        Map<Class<?>, EntityMetadata> inMemoryEntityMetadata = entityMetadataCollector.getInMemoryEntityMetadata();
        BibernateEntityMetadataHolder.setBibernateEntityMetadata(inMemoryEntityMetadata);
    }

    public Persistent(Map<String, String> externalBibernateSettings, HikariDataSource dataSource, String packageName) {
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(externalBibernateSettings,
                null, dataSource);
        enableFlyway();
        BibernateReflectionHolder.setReflection(Persistent.class.getPackageName());
        bibernateDatabaseSettings.setRedisConfiguration(redisConfiguration());
    }

    public BibernateEntityManagerFactory createBibernateEntityManager() {
        return new BibernateEntityManagerFactory(bibernateDatabaseSettings);
    }

    private FlywayConfiguration enableFlyway() {
        return new FlywayConfiguration(bibernateDatabaseSettings);
    }

    private RedisConfiguration redisConfiguration() {
        return new RedisConfiguration(bibernateDatabaseSettings);
    }
}
