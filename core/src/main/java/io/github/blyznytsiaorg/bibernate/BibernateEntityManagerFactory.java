package io.github.blyznytsiaorg.bibernate;

import com.zaxxer.hikari.HikariDataSource;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
@Getter
public class BibernateEntityManagerFactory implements EntityManagerFactory {

    private final BibernateDatabaseSettings bibernateSettings;

    public BibernateEntityManagerFactory(final Map<String, String> bibernateSettings, final String bibernateFileName) {
        this.bibernateSettings = new BibernateDatabaseSettings(bibernateSettings, bibernateFileName);
    }

    public BibernateEntityManagerFactory(final Map<String, String> bibernateSettings,
                                         final String bibernateFileName, HikariDataSource dataSource) {
        this.bibernateSettings = new BibernateDatabaseSettings(bibernateSettings, bibernateFileName, dataSource);
    }

    @Override
    public void close() {
        HikariDataSource dataSource = bibernateSettings.getDataSource();
        if (dataSource != null) {
            log.info("Close dataSource...");
            dataSource.close();
        }
    }

    public BibernateSessionFactory getBibernateSessionFactory() {
        return new BibernateSessionFactory(
                bibernateSettings.getBibernateSettingsProperties(),
                bibernateSettings.getBibernateFileName(),
                bibernateSettings.getDataSource()
        );
    }
}
