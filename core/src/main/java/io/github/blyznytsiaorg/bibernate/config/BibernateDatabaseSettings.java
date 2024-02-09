package io.github.blyznytsiaorg.bibernate.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.blyznytsiaorg.bibernate.cache.RedisConfiguration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

/**
 * Configuration class for Bibernate database settings.
 * It provides methods to create a data source, access database properties, and configure various settings.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Getter
@Slf4j
public class BibernateDatabaseSettings {

    public static final String SHOULD_NOT_BE_NULL_CONFIGURE_BIBERNATE_PROPERTY = " should not be null. Please configure it %s";
    public static final String DB_URL = "db.url";
    public static final String DB_USER = "db.user";
    public static final String DB_PASSWORD = "db.password";
    public static final String DB_MAXIMUM_POOL_SIZE = "db.maxPoolSize";
    private static final String DEFAULT_MAXIMUM_POOL_SIZE = "20";
    private static final String SHOW_SQL = "bibernate.show_sql";
    private static final String DEFAULT_BOOLEAN_FALSE_VALUE = "false";
    private static final String DEFAULT_REDIS_HOST = "localhost";
    private static final String DEFAULT_REDIS_PORT = "6379";
    private static final String COLLECT_QUERIES = "bibernate.collect.queries";
    private static final String FLYWAY_ENABLED = "bibernate.flyway.enabled";
    private static final String SECOND_LEVEL_CACHE = "bibernate.secondLevelCache.enabled";
    private static final String SECOND_LEVEL_CACHE_HOST = "bibernate.secondLevelCache.host";
    private static final String SECOND_LEVEL_CACHE_POST = "bibernate.secondLevelCache.port";
    private static final String BB2DDL_AUTO = "bibernate.2ddl.auto";
    public static final String BIBERNATE_APPLICATION_PROPERTIES = "application.properties";
    public static final String NONE = "none";
    public static final String CREATE = "create";
    private final Map<String, String> bibernateSettingsProperties;
    private final String configurationErrorMessage;
    private final String bibernateFileName;
    private final HikariDataSource dataSource;
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
        this.configurationErrorMessage = configureErrorMessage(bibernateSettingsProperties, bibernateFileName);
        this.bibernateFileName = bibernateFileName;
        this.dataSource = createDataSource();
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
                                     HikariDataSource dataSource) {
        this.bibernateSettingsProperties = bibernateSettingsProperties;
        this.bibernateFileName = bibernateFileName;
        this.configurationErrorMessage = configureErrorMessage(bibernateSettingsProperties, bibernateFileName);
        this.dataSource = dataSource;
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
    private HikariDataSource createDataSource() {
        log.trace("Creating dataSource...");
        String url = bibernateSettingsProperties.get(DB_URL);
        String user = bibernateSettingsProperties.get(DB_USER);
        String password = bibernateSettingsProperties.get(DB_PASSWORD);
        String maxPoolSize = bibernateSettingsProperties.getOrDefault(DB_MAXIMUM_POOL_SIZE, DEFAULT_MAXIMUM_POOL_SIZE);

        Objects.requireNonNull(url, DB_URL + configurationErrorMessage);
        Objects.requireNonNull(user, DB_USER + configurationErrorMessage);
        Objects.requireNonNull(password, DB_PASSWORD + configurationErrorMessage);

        var config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(Integer.parseInt(maxPoolSize));

        return new HikariDataSource(config);
    }

    /**
     * Generates an error message for missing configuration properties.
     *
     * @param bibernateSettings the Bibernate settings properties
     * @param bibernateFileName the name of the Bibernate configuration file
     * @return the error message
     */
    private String configureErrorMessage(Map<String, String> bibernateSettings, String bibernateFileName) {
        String errorMessage;
        if (Objects.nonNull(bibernateSettings)) {
            errorMessage = SHOULD_NOT_BE_NULL_CONFIGURE_BIBERNATE_PROPERTY.formatted(bibernateFileName);
        } else {
            errorMessage = SHOULD_NOT_BE_NULL_CONFIGURE_BIBERNATE_PROPERTY.formatted(BIBERNATE_APPLICATION_PROPERTIES);
        }
        return errorMessage;
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


    private boolean getPropertyBoolean(String key, String defaultValue) {
    /**
     * Retrieves a boolean property value from the Bibernate settings.
     *
     * @param key          the property key
     * @param defaultValue the default value if the property is not found
     * @return the boolean property value
     */
    private boolean getProperty(String key, String defaultValue) {
        return Boolean.parseBoolean(bibernateSettingsProperties.getOrDefault(key, defaultValue));
    }

    private String getPropertyString(String key, String defaultValue) {
        return bibernateSettingsProperties.getOrDefault(key, defaultValue);
    }
}
