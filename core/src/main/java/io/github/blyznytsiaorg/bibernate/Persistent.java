package io.github.blyznytsiaorg.bibernate;

import com.zaxxer.hikari.HikariDataSource;
import io.github.blyznytsiaorg.bibernate.cache.RedisConfiguration;
import io.github.blyznytsiaorg.bibernate.config.BibernateConfiguration;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.config.DDLConfiguration;
import io.github.blyznytsiaorg.bibernate.config.FlywayConfiguration;
import io.github.blyznytsiaorg.bibernate.dao.SimpleRepositoryInvocationHandler;
import io.github.blyznytsiaorg.bibernate.session.BibernateReflectionHolder;
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
    public final String entityWithRepositoriesPackageName;

    public Persistent(String entityWithRepositoriesPackageName) {
        this.entityWithRepositoriesPackageName = entityWithRepositoriesPackageName;
        var bibernateConfiguration = new BibernateConfiguration();
        var bibernateSettings = bibernateConfiguration.load();
        var configFileName = bibernateConfiguration.getConfigFileName();
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(bibernateSettings,
                configFileName);
        enableFlyway();
        processDDLConfiguration(entityWithRepositoriesPackageName);
        BibernateReflectionHolder.setReflection(Persistent.class.getPackageName());
        bibernateDatabaseSettings.setRedisConfiguration(enabledRedisConfiguration());
    }

    public Persistent(String configFileName, String entityWithRepositoriesPackageName) {
        this.entityWithRepositoriesPackageName = entityWithRepositoriesPackageName;
        var bibernateSettings = new BibernateConfiguration(configFileName).load();
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(bibernateSettings,
                configFileName);
        enableFlyway();
        processDDLConfiguration(entityWithRepositoriesPackageName);
        BibernateReflectionHolder.setReflection(Persistent.class.getPackageName());
        bibernateDatabaseSettings.setRedisConfiguration(enabledRedisConfiguration());
    }

    public Persistent(HikariDataSource dataSource, String entityWithRepositoriesPackageName) {
        this.entityWithRepositoriesPackageName = entityWithRepositoriesPackageName;
        var bibernateConfiguration = new BibernateConfiguration();
        var bibernateSettings = bibernateConfiguration.load();
        var configFileName = bibernateConfiguration.getConfigFileName();
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(bibernateSettings,
                configFileName, dataSource);
        enableFlyway();
        processDDLConfiguration(entityWithRepositoriesPackageName);
        BibernateReflectionHolder.setReflection(Persistent.class.getPackageName());
        bibernateDatabaseSettings.setRedisConfiguration(enabledRedisConfiguration());
    }

    public Persistent(String configFileName, HikariDataSource dataSource, String entityWithRepositoriesPackageName) {
        this.entityWithRepositoriesPackageName = entityWithRepositoriesPackageName;
        var bibernateSettings = new BibernateConfiguration(configFileName).load();
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(bibernateSettings,
                configFileName, dataSource);
        enableFlyway();
        processDDLConfiguration(entityWithRepositoriesPackageName);
        BibernateReflectionHolder.setReflection(Persistent.class.getPackageName());
        bibernateDatabaseSettings.setRedisConfiguration(enabledRedisConfiguration());
    }

    public Persistent(Map<String, String> externalBibernateSettings, String entityWithRepositoriesPackageName) {
        this.entityWithRepositoriesPackageName = entityWithRepositoriesPackageName;
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(externalBibernateSettings,
                null);
        enableFlyway();
        processDDLConfiguration(entityWithRepositoriesPackageName);
        BibernateReflectionHolder.setReflection(Persistent.class.getPackageName());
        bibernateDatabaseSettings.setRedisConfiguration(enabledRedisConfiguration());
    }

    public Persistent(Map<String, String> externalBibernateSettings, HikariDataSource dataSource,
                      String entityWithRepositoriesPackageName) {
        this.entityWithRepositoriesPackageName = entityWithRepositoriesPackageName;
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(externalBibernateSettings,
                null, dataSource);
        enableFlyway();
        processDDLConfiguration(entityWithRepositoriesPackageName);
        BibernateReflectionHolder.setReflection(Persistent.class.getPackageName());
        bibernateDatabaseSettings.setRedisConfiguration(enabledRedisConfiguration());
    }

    public BibernateEntityManagerFactory createBibernateEntityManager() {
        return new BibernateEntityManagerFactory(bibernateDatabaseSettings, new SimpleRepositoryInvocationHandler());
    }

    private FlywayConfiguration enableFlyway() {
        return new FlywayConfiguration(bibernateDatabaseSettings);
    }

    private RedisConfiguration enabledRedisConfiguration() {
        return new RedisConfiguration(bibernateDatabaseSettings);
    }

    private DDLConfiguration processDDLConfiguration(String packageName) {
        return new DDLConfiguration(bibernateDatabaseSettings, packageName);
    }

    public StatelessSession createStatelessSession() {
        return new StatelessSession(bibernateDatabaseSettings);
    }
}
