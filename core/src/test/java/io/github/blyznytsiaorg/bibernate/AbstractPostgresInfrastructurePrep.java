package io.github.blyznytsiaorg.bibernate;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractPostgresInfrastructurePrep implements AbstractPostgresTableCreationPrep {

    private static final String POSTGRES_LATEST = "postgres:latest";
    private static final String DB_URL = "db.url";
    private static final String DB_USER = "db.user";
    private static final String DB_PASSWORD = "db.password";
    private static final String DB = "db";
    private static final String USER = "user";
    private static final String PASSWORD = "password";

    @Container
    private final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(POSTGRES_LATEST)
            .withDatabaseName(DB)
            .withUsername(USER)
            .withPassword(PASSWORD);
    public static final String BIBERNATE_FLYWAY_ENABLED = "bibernate.flyway.enabled";
    public static final String DB_MAX_POOL_SIZE = "db.maxPoolSize";
    public static final String POOL_SIZE = "10";
    public static final String BIBERNATE_SHOW_SQL = "bibernate.show_sql";
    public static final String BIBERNATE_COLLECT_QUERIES = "bibernate.collect.queries";

    protected DataSource dataSource;
    protected Map<String, String> bibernateSettings;

    @BeforeEach
    public void setup() {
        postgresContainer.start();
        log.info("Start postgres");

        String jdbcUrl = postgresContainer.getJdbcUrl();
        String databaseName = postgresContainer.getDatabaseName();
        String username = postgresContainer.getUsername();
        String password = postgresContainer.getPassword();

        bibernateSettings = new HashMap<>();

        bibernateSettings.put(DB_URL, jdbcUrl);
        bibernateSettings.put(DB_USER, username);
        bibernateSettings.put(DB_PASSWORD, password);
        bibernateSettings.put(DB_MAX_POOL_SIZE, POOL_SIZE);
        bibernateSettings.put(BIBERNATE_SHOW_SQL, Boolean.TRUE.toString());
        bibernateSettings.put(BIBERNATE_COLLECT_QUERIES, Boolean.TRUE.toString());

        dataSource = createDataSource(jdbcUrl, databaseName, username, password);
    }

    public Persistent createPersistent(String entityPackage) {
        return new Persistent(bibernateSettings, entityPackage);
    }

    public Persistent createPersistentWithFlayWayEnabled(String entityPackage) {
        bibernateSettings.put(BIBERNATE_FLYWAY_ENABLED, Boolean.TRUE.toString());
        return new Persistent(bibernateSettings, entityPackage);
    }

    @AfterEach
    public void tearDown() {
        postgresContainer.stop();
        log.info("Stop postgres");
    }

    private static DataSource createDataSource(String url, String db, String user, String password) {
        var dataSource = new PGSimpleDataSource();
        dataSource.setURL(url);
        dataSource.setDatabaseName(db);
        dataSource.setUser(user);
        dataSource.setPassword(password);

        return dataSource;
    }
}

