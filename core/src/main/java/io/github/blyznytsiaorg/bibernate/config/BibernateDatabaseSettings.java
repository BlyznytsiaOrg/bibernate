package io.github.blyznytsiaorg.bibernate.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

/**
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
    private static final String COLLECT_QUERIES = "bibernate.collect.queries";
    private static final String FLYWAY_ENABLED = "bibernate.flyway.enabled";
    public static final String BIBERNATE_APPLICATION_PROPERTIES = "application.properties";
    private final Map<String, String> bibernateSettingsProperties;
    private final String configurationErrorMessage;
    private final String bibernateFileName;
    private final HikariDataSource dataSource;

    public BibernateDatabaseSettings(Map<String, String> bibernateSettingsProperties, 
                                     String bibernateFileName) {
        this.bibernateSettingsProperties = bibernateSettingsProperties;
        this.configurationErrorMessage = configureErrorMessage(bibernateSettingsProperties, bibernateFileName);
        this.bibernateFileName = bibernateFileName;
        this.dataSource = createDataSource();
    }

    public BibernateDatabaseSettings(Map<String, String> bibernateSettings,
                                     String bibernateFileName, 
                                     HikariDataSource dataSource) {
        this.bibernateSettingsProperties = bibernateSettings;
        this.bibernateFileName = bibernateFileName;
        this.configurationErrorMessage = configureErrorMessage(bibernateSettings, bibernateFileName);
        this.dataSource = dataSource;
    }

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

    private String configureErrorMessage(Map<String, String> bibernateSettings, String bibernateFileName) {
        String errorMessage;
        if (Objects.nonNull(bibernateSettings)) {
            errorMessage = SHOULD_NOT_BE_NULL_CONFIGURE_BIBERNATE_PROPERTY.formatted(bibernateFileName);
        } else {
            errorMessage = SHOULD_NOT_BE_NULL_CONFIGURE_BIBERNATE_PROPERTY.formatted(BIBERNATE_APPLICATION_PROPERTIES);
        }
        return errorMessage;
    }

    public boolean isShowSql() {
        return getProperty(SHOW_SQL, DEFAULT_BOOLEAN_FALSE_VALUE);
    }

    public boolean isCollectQueries() {
        return getProperty(COLLECT_QUERIES, DEFAULT_BOOLEAN_FALSE_VALUE);
    }

    public boolean isFlywayEnabled() {
        return getProperty(FLYWAY_ENABLED, DEFAULT_BOOLEAN_FALSE_VALUE);
    }

    private boolean getProperty(String key, String defaultValue) {
        return Boolean.parseBoolean(bibernateSettingsProperties.getOrDefault(key, defaultValue));
    }
}
