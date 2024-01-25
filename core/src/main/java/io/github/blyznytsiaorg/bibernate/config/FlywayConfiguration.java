package io.github.blyznytsiaorg.bibernate.config;

import static io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings.*;
import static io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings.DB_PASSWORD;
import static io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings.DB_USER;

import org.flywaydb.core.Flyway;
import java.util.Map;

public class FlywayConfiguration {
    public static final String DB_MIGRATION_FILE_NAME = "db.migration";

    public FlywayConfiguration(final BibernateDatabaseSettings bibernateDatabaseSettings) {
        initFlyway(bibernateDatabaseSettings);
    }

    private void initFlyway(BibernateDatabaseSettings bibernateDatabaseSettings) {
        if (bibernateDatabaseSettings.isFlywayEnabled()) {
            Map<String, String> bibernateSettingsProperties
                    = bibernateDatabaseSettings.getBibernateSettingsProperties();

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
