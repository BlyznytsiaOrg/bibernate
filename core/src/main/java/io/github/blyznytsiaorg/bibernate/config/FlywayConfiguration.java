package io.github.blyznytsiaorg.bibernate.config;

import static io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings.*;
import static io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings.DB_PASSWORD;
import static io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings.DB_USER;

import org.flywaydb.core.Flyway;
import java.util.Map;

/**
 * Configures and executes Flyway database migrations based on the provided BibernateDatabaseSettings.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class FlywayConfiguration {
    /**
     * The name of the migration folder.
     */
    public static final String DB_MIGRATION_FILE_NAME = "db.migration";

    /**
     * Initializes Flyway and executes migrations if Flyway is enabled in the Bibernate settings.
     *
     * @param bibernateDatabaseSettings the Bibernate database settings
     */
    public FlywayConfiguration(final BibernateDatabaseSettings bibernateDatabaseSettings) {
        initFlyway(bibernateDatabaseSettings);
    }

    /**
     * Initializes Flyway with the provided BibernateDatabaseSettings and executes migrations if Flyway is enabled.
     *
     * @param bibernateDatabaseSettings the Bibernate database settings
     */
    private void initFlyway(BibernateDatabaseSettings bibernateDatabaseSettings) {
        if (bibernateDatabaseSettings.isFlywayEnabled()) {
            Map<String, String> bibernateSettingsProperties
                    = bibernateDatabaseSettings.getBibernateSettingsProperties();

            var flyway = Flyway.configure()
                    .dataSource(bibernateSettingsProperties.get(DB_URL),
                            bibernateSettingsProperties.get(DB_USER),
                            bibernateSettingsProperties.get(DB_PASSWORD))
                    .locations(DB_MIGRATION_FILE_NAME)
                    .load();

            flyway.migrate();
        }
    }
}
