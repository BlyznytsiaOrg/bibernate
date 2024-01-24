package io.github.blyznytsiaorg.bibernate;

import com.zaxxer.hikari.HikariDataSource;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
@Getter
public class BibernateEntityManagerFactory implements EntityManagerFactory {

    private final BibernateDatabaseSettings bibernateSettings;

    public BibernateEntityManagerFactory(BibernateDatabaseSettings bibernateDatabaseSettings) {
        this.bibernateSettings = bibernateDatabaseSettings;
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
        return new BibernateSessionFactory(bibernateSettings);
    }
}
