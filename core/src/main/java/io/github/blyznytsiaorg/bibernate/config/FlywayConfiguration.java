package io.github.blyznytsiaorg.bibernate.config;

import org.flywaydb.core.Flyway;
import java.util.Map;

public class FlywayConfiguration {
    public static final String DB_URL = "db.url";
    public static final String DB_USER = "db.user";
    public static final String DB_PASSWORD = "db.password";
    private static final String FLYWAY_ENABLED = "bibernate.flyway.enabled";
    public static final String DB_MIGRATION_FILE_NAME = "db.migration";

    public FlywayConfiguration(Map<String, String> bibernateSettingsProperties) {
        initFlyway(bibernateSettingsProperties);
    }

    private void initFlyway(Map<String, String> bibernateSettingsProperties) {
        String flywayEnabled = bibernateSettingsProperties.get(FLYWAY_ENABLED);
        if (Boolean.TRUE.toString().equals(flywayEnabled)) {
            Flyway flyway = Flyway.configure()
                    .dataSource(bibernateSettingsProperties.get(DB_URL),
                            bibernateSettingsProperties.get(DB_USER),
                            bibernateSettingsProperties.get(DB_PASSWORD))
                    .locations(DB_MIGRATION_FILE_NAME)
                    .load();

            flyway.migrate();
        }
    }
}
