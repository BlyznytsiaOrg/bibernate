package io.github.blyznytsiaorg.bibernate;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import javax.sql.DataSource;
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
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>(POSTGRES_LATEST)
            .withDatabaseName(DB)
            .withUsername(USER)
            .withPassword(PASSWORD);

    protected static DataSource dataSource;

    protected static Persistent persistent = new Persistent();

    @BeforeAll
    public static void setup() {
        POSTGRES_CONTAINER.start();
        log.info("Start postgres");

        String jdbcUrl = POSTGRES_CONTAINER.getJdbcUrl();
        String databaseName = POSTGRES_CONTAINER.getDatabaseName();
        String username = POSTGRES_CONTAINER.getUsername();
        String password = POSTGRES_CONTAINER.getPassword();

        Map<String, String> bibernateSettings = persistent.getBibernateSettings();

        bibernateSettings.put(DB_URL, jdbcUrl);
        bibernateSettings.put(DB_USER, username);
        bibernateSettings.put(DB_PASSWORD, password);

        dataSource = createDataSource(jdbcUrl, databaseName, username, password);
    }

    @AfterAll
    public static void tearDown() {
        POSTGRES_CONTAINER.stop();
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

