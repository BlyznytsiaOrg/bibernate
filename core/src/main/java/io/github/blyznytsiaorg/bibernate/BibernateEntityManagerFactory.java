package io.github.blyznytsiaorg.bibernate;

import com.zaxxer.hikari.HikariDataSource;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.dao.SimpleRepositoryInvocationHandler;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactory;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactoryContextHolder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * Implementation of the EntityManagerFactory interface for creating EntityManager instances in the Bibernate framework.
 * The BibernateEntityManagerFactory provides access to the underlying database settings and repository invocation handler.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class BibernateEntityManagerFactory implements EntityManagerFactory {

    private final BibernateDatabaseSettings bibernateSettings;
    private final SimpleRepositoryInvocationHandler simpleRepositoryInvocationHandler;

    /**
     * Closes the underlying resources associated with the EntityManagerFactory, such as the data source.
     */
    @Override
    public void close() {
        HikariDataSource dataSource = bibernateSettings.getDataSource();
        if (dataSource != null) {
            log.trace("Close dataSource...");
            dataSource.close();
        }
    }

    /**
     * Retrieves the BibernateSessionFactory associated with this EntityManagerFactory.
     *
     * @return the BibernateSessionFactory instance
     */
    public BibernateSessionFactory getBibernateSessionFactory() {
        var bibernateSessionFactory = new BibernateSessionFactory(bibernateSettings, simpleRepositoryInvocationHandler);
        BibernateSessionFactoryContextHolder.setBibernateSessionFactory(bibernateSessionFactory);
        return bibernateSessionFactory;
    }
}
