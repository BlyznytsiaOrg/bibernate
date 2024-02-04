package io.github.blyznytsiaorg.bibernate;

import com.zaxxer.hikari.HikariDataSource;
import io.github.blyznytsiaorg.bibernate.config.BibernateConfiguration;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.config.FlywayConfiguration;
import io.github.blyznytsiaorg.bibernate.entity.BibernateEntityMetadataHolder;
import io.github.blyznytsiaorg.bibernate.entity.EntityMetadata;
import io.github.blyznytsiaorg.bibernate.entity.EntityMetadataCollector;
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

    public Persistent(String entityPackage) {
        var bibernateConfiguration = new BibernateConfiguration();
        var bibernateSettings = bibernateConfiguration.load();
        var configFileName = bibernateConfiguration.getConfigFileName();
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(bibernateSettings,
                configFileName);
        enableFlyway();
        EntityMetadataCollector entityMetadataCollector = new EntityMetadataCollector(entityPackage);
        entityMetadataCollector.startCollectMetadata();

        Map<Class<?>, EntityMetadata> inMemoryEntityMetadata = entityMetadataCollector.getInMemoryEntityMetadata();
        BibernateEntityMetadataHolder.setBibernateEntityMetadata(inMemoryEntityMetadata);
    }

    public Persistent(String configFileName, String entityPackage) {
        var bibernateSettings = new BibernateConfiguration(configFileName).load();
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(bibernateSettings,
                configFileName);
        enableFlyway();
    }

    public Persistent(HikariDataSource dataSource) {
        var bibernateConfiguration = new BibernateConfiguration();
        var bibernateSettings = bibernateConfiguration.load();
        var configFileName = bibernateConfiguration.getConfigFileName();
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(bibernateSettings,
                configFileName, dataSource);
        enableFlyway();
    }

    public Persistent(String configFileName, HikariDataSource dataSource) {
        var bibernateSettings = new BibernateConfiguration(configFileName).load();
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(bibernateSettings,
                configFileName, dataSource);
        enableFlyway();
    }

    public Persistent(Map<String, String> externalBibernateSettings, String entityPackage) {
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(externalBibernateSettings,
                null);
        enableFlyway();
        EntityMetadataCollector entityMetadataCollector = new EntityMetadataCollector(entityPackage);
        entityMetadataCollector.startCollectMetadata();
        Map<Class<?>, EntityMetadata> inMemoryEntityMetadata = entityMetadataCollector.getInMemoryEntityMetadata();
        BibernateEntityMetadataHolder.setBibernateEntityMetadata(inMemoryEntityMetadata);
    }

    public Persistent(Map<String, String> externalBibernateSettings, HikariDataSource dataSource) {
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(externalBibernateSettings,
                null, dataSource);
        enableFlyway();
    }

    public BibernateEntityManagerFactory createBibernateEntityManager() {
        return new BibernateEntityManagerFactory(bibernateDatabaseSettings);
    }

    private FlywayConfiguration enableFlyway() {
        return new FlywayConfiguration(bibernateDatabaseSettings);
    }
}
