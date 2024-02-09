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
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class BibernateSessionFactory extends BibernateEntityManagerFactory {

    private EntityDao entityDao;

    public BibernateSessionFactory(BibernateDatabaseSettings bibernateDatabaseSettings,
                                   SimpleRepositoryInvocationHandler simpleRepositoryInvocationHandler) {
        super(bibernateDatabaseSettings, simpleRepositoryInvocationHandler);
    }

    public BibernateSession openSession() {
        this.entityDao = entityDao();
        var jdbcBibernateSession = new DefaultBibernateSession(entityDao);

        BibernateSession bibernateSession;

        if (getBibernateSettings().isSecondLevelCacheEnabled()) {
            var redisConfiguration = getBibernateSettings().getRedisConfiguration();
            var bibernateSecondLevelCacheSession = new BibernateSecondLevelCacheSession(
                    jdbcBibernateSession, redisConfiguration.getDistributedMap()
            );

            bibernateSession = new CloseBibernateSession(new BibernateFirstLevelCacheSession(
                    bibernateSecondLevelCacheSession, new DefaultActionQueue())
            );
        } else {
            bibernateSession = new CloseBibernateSession(new BibernateFirstLevelCacheSession(
                    jdbcBibernateSession, new DefaultActionQueue())
            );
        }

        setBibernateSession(bibernateSession);
        return bibernateSession;
    }

    private EntityDao entityDao() {
        List<String> executedQueries = new ArrayList<>();
        Identity identity = new PostgresIdentity(getBibernateSettings(), executedQueries);
        return new EntityDao(new SqlBuilder(), getBibernateSettings(), identity, executedQueries);
    }

    public List<String> getExecutedQueries() {
        if (Objects.isNull(entityDao)) {
            return Collections.emptyList();
        }
        return entityDao.getExecutedQueries();
    }
}
