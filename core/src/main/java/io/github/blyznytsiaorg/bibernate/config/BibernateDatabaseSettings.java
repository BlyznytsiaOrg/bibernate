package io.github.blyznytsiaorg.bibernate.config;

import io.github.blyznytsiaorg.bibernate.cache.RedisConfiguration;
import io.github.blyznytsiaorg.bibernate.connectionpool.BibernateDatasSourceConfig;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.transaction.TransactionalDatasource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

/**
 * Configuration class for Bibernate database settings.
 * It provides methods to create a data source, access database properties, and configure various settings.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Getter
@Slf4j
public class BibernateDatabaseSettings {

    public static final String DB_URL = "db.url";
    public static final String DB_USER = "db.user";
    public static final String DB_PASSWORD = "db.password";
    public static final String DB_MAXIMUM_POOL_SIZE = "db.maxPoolSize";
    private static final String SHOW_SQL = "bibernate.show_sql";
    private static final String BATCH_SIZE = "bibernate.batch_size";
    private static final String COLLECT_QUERIES = "bibernate.collect.queries";
    private static final String FLYWAY_ENABLED = "bibernate.flyway.enabled";
    private static final String SECOND_LEVEL_CACHE = "bibernate.secondLevelCache.enabled";
    private static final String SECOND_LEVEL_CACHE_HOST = "bibernate.secondLevelCache.host";
    private static final String SECOND_LEVEL_CACHE_POST = "bibernate.secondLevelCache.port";
    private static final String BB2DDL_AUTO = "bibernate.2ddl.auto";
    public static final String BIBERNATE_APPLICATION_PROPERTIES = "application.properties";

    private static final String DEFAULT_BOOLEAN_FALSE_VALUE = "false";
    private static final String DEFAULT_REDIS_HOST = "localhost";
    private static final String DEFAULT_REDIS_PORT = "6379";
    private static final String DEFAULT_BATCH_SIZE = "1";
    private static final String DEFAULT_MAXIMUM_POOL_SIZE = "20";
    public static final String NONE = "none";
    public static final String CREATE = "create";
    private static final String DEFAULT_DB_CONNECTION_URL = "jdbc:postgresql://localhost:5432/db";
    private static final String DEFAULT_DB_USERNAME = "user";
    private static final String DEFAULT_DB_PASSWORD = "password";

    private final Map<String, String> bibernateSettingsProperties;
    private final String bibernateFileName;
    private final TransactionalDatasource dataSource;
    private RedisConfiguration redisConfiguration;

    /**
     * Constructs a new BibernateDatabaseSettings instance with the specified Bibernate settings properties and file name.
     *
     * @param bibernateSettingsProperties the Bibernate settings properties loaded from a configuration file
     * @param bibernateFileName           the name of the configuration file
     */
    public BibernateDatabaseSettings(Map<String, String> bibernateSettingsProperties,
                                     String bibernateFileName) {
        this.bibernateSettingsProperties = bibernateSettingsProperties;
        this.bibernateFileName = bibernateFileName;
        this.dataSource = createDataSource();
        checkDatabaseSettings();
    }

    /**
     * Constructs a new BibernateDatabaseSettings instance with the specified Bibernate settings properties,
     * file name, and data source.
     *
     * @param bibernateSettingsProperties the Bibernate settings properties loaded from a configuration file
     * @param bibernateFileName           the name of the configuration file
     * @param dataSource                  the data source to be used
     */
    public BibernateDatabaseSettings(Map<String, String> bibernateSettingsProperties,
                                     String bibernateFileName,
                                     TransactionalDatasource dataSource) {
        this.bibernateSettingsProperties = bibernateSettingsProperties;
        this.bibernateFileName = bibernateFileName;
        this.dataSource = dataSource;
        checkDatabaseSettings();
    }

    /**
     * Sets the Redis configuration for distributed caching.
     *
     * @param redisConfiguration the Redis configuration to be set
     */
    public void setRedisConfiguration(RedisConfiguration redisConfiguration) {
        this.redisConfiguration = redisConfiguration;
    }

    /**
     * Creates a Hikari data source using the configured properties.
     *
     * @return the HikariDataSource object
     */
    private TransactionalDatasource createDataSource() {
        log.trace("Creating dataSource...");
        String url = bibernateSettingsProperties.getOrDefault(DB_URL, DEFAULT_DB_CONNECTION_URL);
        String user = bibernateSettingsProperties.getOrDefault(DB_USER, DEFAULT_DB_USERNAME);
        String password = bibernateSettingsProperties.getOrDefault(DB_PASSWORD, DEFAULT_DB_PASSWORD);
        String maxPoolSize = bibernateSettingsProperties.getOrDefault(DB_MAXIMUM_POOL_SIZE, DEFAULT_MAXIMUM_POOL_SIZE);

        var config = BibernateDatasSourceConfig.builder()
                .jdbcUrl(url)
                .username(user)
                .password(password)
                .maximumPoolSize(Integer.parseInt(maxPoolSize))
                .build();

        return new TransactionalDatasource(config);
    }

    /**
     * Checks if SQL logging is enabled.
     *
     * @return true if SQL logging is enabled, otherwise false
     */
    public boolean isShowSql() {
        return getPropertyBoolean(SHOW_SQL, DEFAULT_BOOLEAN_FALSE_VALUE);
    }

    /**
     * Checks if query collection is enabled.
     *
     * @return true if query collection is enabled, otherwise false
     */
    public boolean isCollectQueries() {
        return getPropertyBoolean(COLLECT_QUERIES, DEFAULT_BOOLEAN_FALSE_VALUE);
    }

    /**
     * Checks if Flyway migration is enabled.
     *
     * @return true if Flyway migration is enabled, otherwise false
     */
    public boolean isFlywayEnabled() {
        return getPropertyBoolean(FLYWAY_ENABLED, DEFAULT_BOOLEAN_FALSE_VALUE);
    }

    /**
     * Checks if DDL auto creation is enabled.
     *
     * @return true if DDL auto create is enabled, otherwise false
     */
    public boolean isDDLCreate() {
        String ddlProperty = getPropertyString(BB2DDL_AUTO, NONE);
        return ddlProperty.equals(CREATE);
    }

    /**
     * Checks if the second level cache is enabled.
     *
     * @return true if the second level cache is enabled, otherwise false
     */
    public boolean isSecondLevelCacheEnabled() {
        return getPropertyBoolean(SECOND_LEVEL_CACHE, DEFAULT_BOOLEAN_FALSE_VALUE);
    }

    /**
     * Gets the host of the second level cache (Redis).
     *
     * @return the host of the second level cache
     */
    public String getSecondLevelCacheHost() {
        return bibernateSettingsProperties.getOrDefault(SECOND_LEVEL_CACHE_HOST, DEFAULT_REDIS_HOST);
    }

    /**
     * Gets the port of the second level cache (Redis).
     *
     * @return the port of the second level cache
     */
    public int getSecondLevelCachePost() {
        return Integer.parseInt(bibernateSettingsProperties.getOrDefault(SECOND_LEVEL_CACHE_POST, DEFAULT_REDIS_PORT));
    }

    /**
     * Retrieves the batch size configuration for batch processing from the Bibernate settings properties.
     * If the batch size is not explicitly configured, the method returns the default batch size value.
     *
     * @return The configured batch size for batch processing or the default batch size if not explicitly set.
     */
    public int getBatchSize() {
        return Integer.parseInt(bibernateSettingsProperties.getOrDefault(BATCH_SIZE, DEFAULT_BATCH_SIZE));
    }

    /**
     * Retrieves a boolean property value from the Bibernate settings.
     *
     * @param key          the property key
     * @param defaultValue the default value if the property is not found
     * @return the boolean property value
     */
    private boolean getPropertyBoolean(String key, String defaultValue) {
        return Boolean.parseBoolean(bibernateSettingsProperties.getOrDefault(key, defaultValue));
    }

    /**
     * Retrieves a String property value from the Bibernate settings.
     *
     * @param key          the property key
     * @param defaultValue the default value if the property is not found
     * @return the String property value
     */
    private String getPropertyString(String key, String defaultValue) {
        return bibernateSettingsProperties.getOrDefault(key, defaultValue);
    }

    /**
     * Checks database settings to ensure they are consistent.
     * If Flyway is enabled and DDL auto is set to "create",
     * it throws a BibernateGeneralException.
     *
     * @throws BibernateGeneralException
     */
    private void checkDatabaseSettings() {
        boolean flywayEnabled = getPropertyBoolean(FLYWAY_ENABLED, DEFAULT_BOOLEAN_FALSE_VALUE);
        String ddlCreateProperty = getPropertyString(BB2DDL_AUTO, NONE);
        if (flywayEnabled && ddlCreateProperty.equals(CREATE)) {
            throw new BibernateGeneralException("Configuration error: bibernate.flyway.enabled=true "
                    + "and bibernate.2ddl.auto=create. Choose one property for creating schema");
        }
    }
}
