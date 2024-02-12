package io.github.blyznytsiaorg.bibernate.datasource;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.blyznytsiaorg.bibernate.connectionpool.BibernateDataSource;
import io.github.blyznytsiaorg.bibernate.connectionpool.BibernateDatasSourceConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;

@Slf4j
class BibernateDataSourceTest {
    private static final String POSTGRES_LATEST = "postgres:latest";
    private static final String DB = "db";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    public static final int MAXIMUM_POOL_SIZE = 10;
    private static final int ROWS_SIZE = 100;
    protected DataSource dataSource;

    @Container
    private final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(POSTGRES_LATEST)
            .withDatabaseName(DB)
            .withUsername(USER)
            .withPassword(PASSWORD);

    @BeforeEach
    public void setup() {
        postgresContainer.start();
        log.info("Start postgres");

        String jdbcUrl = postgresContainer.getJdbcUrl();
        String username = postgresContainer.getUsername();
        String password = postgresContainer.getPassword();
        dataSource = createDataSource(jdbcUrl, username, password);

        createTable();
    }

    @Test
    @DisplayName("quantity of connections should be less or equals pool size that is 10")
    void shouldUseConnectionsFromPool() {
        Set<Connection> connections = new HashSet<>();
        String insertQuery = """
                INSERT INTO users (name, age) 
                values (?, ?);
                """;
        for (int i = 0; i < ROWS_SIZE; i++) {
            try (var connection = dataSource.getConnection()) {
                try (var statement = connection.prepareStatement(insertQuery)) {
                    connections.add(connection);
                    statement.setString(1, "name" + i);
                    statement.setInt(2, i);
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        assertThat(connections).hasSizeLessThanOrEqualTo(MAXIMUM_POOL_SIZE);

        String countQuery = "SELECT count(u.id) from users u;";
        long actualRows = 0;
        try (var connection = dataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(countQuery);
                if (resultSet.next()) {
                    actualRows = resultSet.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        assertThat(actualRows).isEqualTo(ROWS_SIZE);
    }

    @AfterEach
    public void tearDown() {
        dropTable();
        postgresContainer.stop();
        log.info("Stop postgres");
    }

    private static DataSource createDataSource(String url, String user, String password) {
        BibernateDatasSourceConfig config = BibernateDatasSourceConfig.builder()
                .jdbcUrl(url)
                .username(user)
                .password(password)
                .maximumPoolSize(MAXIMUM_POOL_SIZE)
                .build();
        return new BibernateDataSource(config);
    }

    private void createTable() {
        String createQuery = """
                CREATE TABLE IF NOT EXISTS users (
                id bigserial PRIMARY KEY,
                name varchar (50) not null,
                age int not null);
                 """;
        try (var connection = dataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                statement.execute(createQuery);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void dropTable() {
        String dropQuery = """
                DROP TABLE users;
                 """;
        try (var connection = dataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                statement.execute(dropQuery);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
