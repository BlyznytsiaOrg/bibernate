package io.github.blyznytsiaorg.bibernate;

import com.zaxxer.hikari.HikariDataSource;
import io.github.blyznytsiaorg.bibernate.config.BibernateConfiguration;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.config.FlywayConfiguration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Getter
@Slf4j
public class Persistent {
    private final BibernateDatabaseSettings bibernateDatabaseSettings;

    public Persistent() {
        var bibernateConfiguration = new BibernateConfiguration();
        var bibernateSettings = bibernateConfiguration.load();
        var configFileName = bibernateConfiguration.getConfigFileName();
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(bibernateSettings,
                configFileName);
        enableFlyway();
    }

    public Persistent(String configFileName) {
        var bibernateSettings = new BibernateConfiguration(configFileName).load();
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(bibernateSettings,
                configFileName);
        enableFlyway();
    }

    public Persistent(HikariDataSource dataSource) {
        var bibernateConfiguration = new BibernateConfiguration();
        var bibernateSettings = bibernateConfiguration.load();
        var configFileName = bibernateConfiguration.getConfigFileName();
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(bibernateSettings,
                configFileName, dataSource);
        enableFlyway();
    }

    public Persistent(String configFileName, HikariDataSource dataSource) {
        var bibernateSettings = new BibernateConfiguration(configFileName).load();
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(bibernateSettings,
                configFileName, dataSource);
        enableFlyway();
    }

    public Persistent(Map<String, String> externalBibernateSettings) {
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(externalBibernateSettings,
                null);
        enableFlyway();
    }

    public Persistent(Map<String, String> externalBibernateSettings, HikariDataSource dataSource) {
        this.bibernateDatabaseSettings = new BibernateDatabaseSettings(externalBibernateSettings,
                null, dataSource);
        enableFlyway();
    }

    public BibernateEntityManagerFactory createBibernateEntityManager() {
        return new BibernateEntityManagerFactory(bibernateDatabaseSettings);
    }

    private FlywayConfiguration enableFlyway() {
        return new FlywayConfiguration(bibernateDatabaseSettings);
    }
}
