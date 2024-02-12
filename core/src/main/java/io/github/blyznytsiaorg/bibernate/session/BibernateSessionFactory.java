package io.github.blyznytsiaorg.bibernate.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.github.blyznytsiaorg.bibernate.BibernateEntityManagerFactory;
import io.github.blyznytsiaorg.bibernate.actionqueue.impl.DefaultActionQueue;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.dao.EntityDao;
import io.github.blyznytsiaorg.bibernate.dao.SimpleRepositoryInvocationHandler;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.identity.Identity;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.identity.PostgresIdentity;

import static io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder.*;

/**
 * Represents a session factory in the Bibernate framework, responsible for creating sessions to interact with the database.
 * Extends {@link BibernateEntityManagerFactory} to inherit configuration settings.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class BibernateSessionFactory extends BibernateEntityManagerFactory {

    private EntityDao entityDao;

    /**
     * Constructs a new BibernateSessionFactory with the provided database settings and repository invocation handler.
     *
     * @param bibernateDatabaseSettings        the database settings for configuring the session factory
     * @param simpleRepositoryInvocationHandler the repository invocation handler for handling repository methods
     */
    public BibernateSessionFactory(BibernateDatabaseSettings bibernateDatabaseSettings,
                                   SimpleRepositoryInvocationHandler simpleRepositoryInvocationHandler) {
        super(bibernateDatabaseSettings, simpleRepositoryInvocationHandler);
    }

    /**
     * Opens a new session for interacting with the database.
     *
     * @return a new BibernateSession instance
     */
    public BibernateSession openSession() {
        this.entityDao = entityDao();
        var jdbcBibernateSession = new DefaultBibernateSession(entityDao);

        BibernateSession bibernateSession;

        if (getBibernateSettings().isSecondLevelCacheEnabled()) {
            var redisConfiguration = getBibernateSettings().getRedisConfiguration();
            var bibernateSecondLevelCacheSession = new BibernateSecondLevelCacheSession(
                    jdbcBibernateSession, redisConfiguration.getDistributedMap()
            );

            bibernateSession = new ValidatorBibernateSession(new CloseBibernateSession(new BibernateFirstLevelCacheSession(
                    bibernateSecondLevelCacheSession, new DefaultActionQueue())
            ));
        } else {
            bibernateSession = new ValidatorBibernateSession(new CloseBibernateSession(new BibernateFirstLevelCacheSession(
                    jdbcBibernateSession, new DefaultActionQueue())
            ));
        }

        setBibernateSession(bibernateSession);
        return bibernateSession;
    }

    /**
     * Creates a new EntityDao instance based on the configured database settings.
     *
     * @return a new EntityDao instance
     */
    private EntityDao entityDao() {
        List<String> executedQueries = new ArrayList<>();
        Identity identity = new PostgresIdentity(getBibernateSettings(), executedQueries);
        return new EntityDao(new SqlBuilder(), getBibernateSettings(), identity, executedQueries);
    }

    /**
     * Retrieves the list of executed queries during the session, if available.
     *
     * @return the list of executed queries, or an empty list if no queries were executed
     */
    public List<String> getExecutedQueries() {
        if (Objects.isNull(entityDao)) {
            return Collections.emptyList();
        }
        return entityDao.getExecutedQueries();
    }
}
