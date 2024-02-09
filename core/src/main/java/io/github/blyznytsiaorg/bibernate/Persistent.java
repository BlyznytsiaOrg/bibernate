package io.github.blyznytsiaorg.bibernate;

import io.github.blyznytsiaorg.bibernate.cache.RedisConfiguration;
import io.github.blyznytsiaorg.bibernate.config.BibernateConfiguration;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.config.DDLConfiguration;
import io.github.blyznytsiaorg.bibernate.config.FlywayConfiguration;
import io.github.blyznytsiaorg.bibernate.dao.SimpleRepositoryInvocationHandler;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadataCollector;
import io.github.blyznytsiaorg.bibernate.transaction.TransactionalDatasource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder.setBibernateEntityMetadata;
import static io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder.setReflection;
import static java.util.Objects.requireNonNull;

/**
 * The `Persistent` class provides methods for configuring and creating Bibernate entity managers, stateless sessions, and enabling Flyway migrations and Redis caching.
 * It allows for both default and external configurations, enabling flexibility in setting up the persistence layer.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Getter
@Slf4j
public class Persistent {
    private final BibernateDatabaseSettings bibernateDatabaseSettings;
    private final String entitiesPackageName;

    /**
     * Constructs a `Persistent` instance with default configuration.
     *
     * @param entitiesPackageName   the package name where entities are located
     * @return                      a `Persistent` instance with default configuration
     */
    public static Persistent withDefaultConfiguration(String entitiesPackageName) {
        var bibernateConfiguration = new BibernateConfiguration();
        var internalBibernateSettings = bibernateConfiguration.load();
        var configFileName = bibernateConfiguration.getConfigFileName();
        return new Persistent(entitiesPackageName, internalBibernateSettings, configFileName);
    }

    /**
     * Constructs a `Persistent` instance with external configuration.
     *
     * @param entitiesPackageName       the package name where entities are located
     * @param externalBibernateSettings the external Hibernate settings
     * @param configFileName            the configuration file name
     * @return                          a `Persistent` instance with external configuration
     */
    public static Persistent withExternalConfiguration(String entitiesPackageName,
                                                Map<String, String> externalBibernateSettings, String configFileName) {
        return new Persistent(entitiesPackageName, externalBibernateSettings, configFileName);
    }

    /**
     * Constructs a `Persistent` instance with external configuration and a custom data source.
     *
     * @param entitiesPackageName       the package name where entities are located
     * @param externalBibernateSettings the external Hibernate settings
     * @param configFileName            the configuration file name
     * @param dataSource                the custom data source
     * @return                          a `Persistent` instance with external configuration and a custom data source
     */
    public static Persistent withExternalConfigurationAndDataSource(
            String entitiesPackageName, Map<String, String> externalBibernateSettings,
            String configFileName, TransactionalDatasource dataSource) {
        return new Persistent(entitiesPackageName, externalBibernateSettings, configFileName, dataSource);
    }

    /**
     * Constructs a `Persistent` instance with external Hibernate settings and the entities package name.
     *
     * @param externalBibernateSettings the external Hibernate settings
     * @param entitiesPackageName       the package name where entities are located
     */
    public Persistent(String entitiesPackageName, Map<String, String> externalBibernateSettings, String configFileName) {
        requireNonNull(entitiesPackageName, "EntitiesPackageName should not be null");
        requireNonNull(externalBibernateSettings, "externalBibernateSettings should not be null");
        requireNonNull(configFileName, "configFileName should not be null");

        this.entitiesPackageName = entitiesPackageName;
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(externalBibernateSettings, configFileName);
        setReflection(Persistent.class.getPackageName());

        enableFlyway();
        processDDLConfiguration(entitiesPackageName);
        bibernateDatabaseSettings.setRedisConfiguration(enabledRedisConfiguration());

        var entityMetadataCollector = new EntityMetadataCollector(entitiesPackageName);
        Map<Class<?>, EntityMetadata> classEntityMetadataMap = entityMetadataCollector.collectMetadata();
        setBibernateEntityMetadata(classEntityMetadataMap);
    }

    /**
     * Constructs a `Persistent` instance with external Hibernate settings, the entities package name, and a custom data source.
     *
     * @param entitiesPackageName       the package name where entities are located
     * @param externalBibernateSettings the external Hibernate settings
     * @param configFileName            the configuration file name
     * @param dataSource                the custom data source
     * @throws NullPointerException if any of the arguments is null
     */
    public Persistent(String entitiesPackageName, Map<String, String> externalBibernateSettings,
                      String configFileName, TransactionalDatasource dataSource) {
        requireNonNull(entitiesPackageName, "EntitiesPackageName should not be null");
        requireNonNull(externalBibernateSettings, "externalBibernateSettings should not be null");
        requireNonNull(configFileName, "configFileName should not be null");

        this.entitiesPackageName = entitiesPackageName;
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(externalBibernateSettings, configFileName, dataSource);
        setReflection(Persistent.class.getPackageName());

        enableFlyway();
        processDDLConfiguration(entitiesPackageName);
        bibernateDatabaseSettings.setRedisConfiguration(enabledRedisConfiguration());

        var entityMetadataCollector = new EntityMetadataCollector(entitiesPackageName);
        Map<Class<?>, EntityMetadata> classEntityMetadataMap = entityMetadataCollector.collectMetadata();
        setBibernateEntityMetadata(classEntityMetadataMap);
    }

    /**
     * Creates a new instance of {@link BibernateEntityManagerFactory}.
     *
     * @return a new instance of {@link BibernateEntityManagerFactory}
     */
    public BibernateEntityManagerFactory createBibernateEntityManager() {
        return new BibernateEntityManagerFactory(bibernateDatabaseSettings, new SimpleRepositoryInvocationHandler());
    }

    /**
     * Enables Flyway configuration.
     *
     * @return a new instance of {@link FlywayConfiguration}
     */
    private FlywayConfiguration enableFlyway() {
        return new FlywayConfiguration(bibernateDatabaseSettings);
    }

    /**
     * Enables Redis configuration.
     *
     * @return a new instance of {@link RedisConfiguration}
     */
    private RedisConfiguration enabledRedisConfiguration() {
        return new RedisConfiguration(bibernateDatabaseSettings);
    }

    /**
     * Processes DDL configuration for the specified package.
     *
     * @param packageName the package name for which DDL configuration is processed
     * @return a new instance of {@link DDLConfiguration}
     */
    private DDLConfiguration processDDLConfiguration(String packageName) {
        return new DDLConfiguration(bibernateDatabaseSettings, packageName);
    }

    /**
     * Creates a new stateless session.
     *
     * @return a new instance of {@link StatelessSession}
     */
    public StatelessSession createStatelessSession() {
        return new StatelessSession(bibernateDatabaseSettings);
    }
}
