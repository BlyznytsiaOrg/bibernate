package io.github.blyznytsiaorg.bibernate.session;

import io.github.blyznytsiaorg.bibernate.BibernateEntityManagerFactory;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.dao.EntityDao;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.identity.Identity;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.identity.PostgresIdentity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class BibernateSessionFactory extends BibernateEntityManagerFactory {

    private EntityDao entityDao;

    public BibernateSessionFactory(BibernateDatabaseSettings bibernateDatabaseSettings) {
        super(bibernateDatabaseSettings);
    }

    public BibernateSession openSession() {
        this.entityDao = entityDao();
        var session = new BibernateFirstLevelCacheSession(new DefaultBibernateSession(entityDao));
        BibernateSessionContextHolder.setBibernateSession(session);
        return session;
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
