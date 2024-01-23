package io.github.blyznytsiaorg.bibernate.session;

import com.zaxxer.hikari.HikariDataSource;
import io.github.blyznytsiaorg.bibernate.BibernateEntityManagerFactory;
import io.github.blyznytsiaorg.bibernate.dao.EntityDao;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class BibernateSessionFactory extends BibernateEntityManagerFactory {

    private EntityDao entityDao;

    public BibernateSessionFactory(Map<String, String> bibernateSettings, String bibernateFileName) {
        super(bibernateSettings, bibernateFileName);
    }

    public BibernateSessionFactory(Map<String, String> bibernateSettings, String bibernateFileName, HikariDataSource dataSource) {
        super(bibernateSettings, bibernateFileName, dataSource);
    }

    public BibernateSession openSession() {
        this.entityDao = entityDao();
        var session = new BibernateFirstLevelCacheSession(new DefaultBibernateSession(entityDao));
        BibernateSessionContextHolder.setBibernateSession(session);
        return session;
    }

    public EntityDao entityDao() {
        return new EntityDao(new SqlBuilder(), getBibernateSettings());
    }

    public List<String> getExecutedQueries() {
        if (Objects.isNull(entityDao)) {
            return Collections.emptyList();
        }
        return entityDao.getExecutedQueries();
    }
}
