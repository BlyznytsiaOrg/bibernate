package io.github.blyznytsiaorg.bibernate;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import redis.clients.jedis.Jedis;

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
    private static final String REDIS_LATEST = "redis:latest";
    private static final int REDIS_DEFAULT_PORT = 6379;
    public static final String PACKAGE_NAME = "testdata";
    public static final String PACKAGE_NAME_FOR_DDL = "testdata.entity";
    public static final String PACKAGE_NAME_FOR_INDEX_COLUMN_LIST_MISMATCH = "testdata.mismatchcolumnlistindex";
    public static final String PACKAGE_NAME_FOR_NO_RELATION_FOUND= "testdata.notexistedrelation";
    public static final String PACKAGE_NAME_FOR_NO_MANY_TO_MANY_ON_JOIN_TABLE= "testdata.jointablewithoutmanytomany";
    public static final String PACKAGE_NAME_FOR_JOIN_COLUMN_MAPPING_EXCEPTION = "testdata.joincolumnmappingexception";
    public static final String CREATE = "create";

    @Container
    private final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(POSTGRES_LATEST)
            .withDatabaseName(DB)
            .withUsername(USER)
            .withPassword(PASSWORD);

    @Container
    private final GenericContainer<?> redisContainer = new GenericContainer<>(REDIS_LATEST)
            .withExposedPorts(REDIS_DEFAULT_PORT);

    public static final String BIBERNATE_FLYWAY_ENABLED = "bibernate.flyway.enabled";
    public static final String BB2DDL_AUTO = "bibernate.2ddl.auto";
    public static final String SECOND_LEVEL_CACHE = "bibernate.secondLevelCache.enabled";
    private static final String SECOND_LEVEL_CACHE_HOST = "bibernate.secondLevelCache.host";
    private static final String SECOND_LEVEL_CACHE_POST = "bibernate.secondLevelCache.port";
    public static final String DB_MAX_POOL_SIZE = "db.maxPoolSize";
    public static final String POOL_SIZE = "10";
    public static final String BIBERNATE_SHOW_SQL = "bibernate.show_sql";
    public static final String DEFAULT_BATCH_SIZE = "2";
    public static final String BATCH_SIZE = "bibernate.batch_size";
    public static final String BIBERNATE_COLLECT_QUERIES = "bibernate.collect.queries";

    protected DataSource dataSource;
    protected Map<String, String> bibernateSettings;

    protected Jedis jedis;

    @BeforeEach
    public void setup() {
        postgresContainer.start();
        log.info("Start postgres");
        redisContainer.start();
        log.info("Start redis");

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
        bibernateSettings.put(BATCH_SIZE, DEFAULT_BATCH_SIZE);
        bibernateSettings.put(BIBERNATE_COLLECT_QUERIES, Boolean.TRUE.toString());

        dataSource = createDataSource(jdbcUrl, databaseName, username, password);

        String redisHost = redisContainer.getHost();
        Integer redisPort = redisContainer.getFirstMappedPort();

        bibernateSettings.put(SECOND_LEVEL_CACHE_HOST, redisHost);
        bibernateSettings.put(SECOND_LEVEL_CACHE_POST, String.valueOf(redisPort));

        jedis = new Jedis(redisHost, redisPort);
    }

    public Persistent createPersistent(String packageName) {
        return new Persistent(bibernateSettings, packageName);
    }

    public Persistent createPersistent() {
        return new Persistent(bibernateSettings, PACKAGE_NAME);
    }

    public Persistent createPersistentWithFlayWayEnabled() {
        bibernateSettings.put(BIBERNATE_FLYWAY_ENABLED, Boolean.TRUE.toString());
        return new Persistent(bibernateSettings, PACKAGE_NAME);
    }

    public Persistent createPersistentWithFlayWayEnabled(String packageName) {
        bibernateSettings.put(BIBERNATE_FLYWAY_ENABLED, Boolean.TRUE.toString());
        return new Persistent(bibernateSettings, packageName);
    }

    public Persistent createPersistentWithBb2ddlCreate() {
        bibernateSettings.put(BB2DDL_AUTO, CREATE);
        return new Persistent(bibernateSettings, PACKAGE_NAME_FOR_DDL);
    }

    public Persistent createPersistentWithBb2ddlCreate(String packageName) {
        bibernateSettings.put(BB2DDL_AUTO, CREATE);
        return new Persistent(bibernateSettings, packageName);
    }

    public Persistent createPersistentIndexColumnListMismatch() {
        bibernateSettings.put(BB2DDL_AUTO, CREATE);
        return new Persistent(bibernateSettings, PACKAGE_NAME_FOR_INDEX_COLUMN_LIST_MISMATCH);
    }

    public Persistent createPersistentNoRelationFound() {
        bibernateSettings.put(BB2DDL_AUTO, CREATE);
        return new Persistent(bibernateSettings, PACKAGE_NAME_FOR_NO_RELATION_FOUND);
    }

    public Persistent createPersistentNoManyToManyOnJoinTable() {
        bibernateSettings.put(BB2DDL_AUTO, CREATE);
        return new Persistent(bibernateSettings, PACKAGE_NAME_FOR_NO_MANY_TO_MANY_ON_JOIN_TABLE);
    }

    public Persistent createPersistentJoinColumnMappingException() {
        bibernateSettings.put(BB2DDL_AUTO, CREATE);
        return new Persistent(bibernateSettings, PACKAGE_NAME_FOR_JOIN_COLUMN_MAPPING_EXCEPTION);
    }

    public Persistent createPersistentWithSecondLevelCache() {
        bibernateSettings.put(SECOND_LEVEL_CACHE, Boolean.TRUE.toString());
        return new Persistent(bibernateSettings, PACKAGE_NAME);
    }

    @AfterEach
    public void tearDown() {
        postgresContainer.stop();
        log.info("Stop postgres");
        jedis.close();
        redisContainer.stop();
        log.info("Stop redis");
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

