package io.github.blyznytsiaorg.bibernate;

import com.zaxxer.hikari.HikariDataSource;
import io.github.blyznytsiaorg.bibernate.config.BibernateConfiguration;
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
    private final Map<String, String> bibernateSettings;
    private final String configFileName;

    public Persistent() {
        var bibernateConfiguration = new BibernateConfiguration();
        this.bibernateSettings = bibernateConfiguration.load();
        this.configFileName = bibernateConfiguration.getConfigFileName();
    }

    public Persistent(String configFileName) {
        bibernateSettings = new BibernateConfiguration(configFileName).load();
        this.configFileName = configFileName;
    }

    public Persistent(Map<String, String> externalBibernateSettings) {
        this.bibernateSettings = externalBibernateSettings;
        this.configFileName = null;
    }

    public BibernateEntityManagerFactory createBibernateEntityManager() {
        return new BibernateEntityManagerFactory(bibernateSettings, configFileName);
    }

    public BibernateEntityManagerFactory createBibernateEntityManager(HikariDataSource dataSource) {
        return new BibernateEntityManagerFactory(bibernateSettings, configFileName, dataSource);
    }
}
