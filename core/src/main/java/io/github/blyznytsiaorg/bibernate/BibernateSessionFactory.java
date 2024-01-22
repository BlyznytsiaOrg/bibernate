package io.github.blyznytsiaorg.bibernate;

import com.zaxxer.hikari.HikariDataSource;
import io.github.blyznytsiaorg.bibernate.dao.EntityDao;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder;
import io.github.blyznytsiaorg.bibernate.entity.EntityMapper;

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
        return new BibernateFirstLevelCacheSession(new DefaultBibernateSession(entityDao));
    }

    public EntityDao entityDao() {
        return new EntityDao(new SqlBuilder(), new EntityMapper(), getBibernateSettings());
    }

    public List<String> getExecutedQueries() {
        if (Objects.isNull(entityDao)) {
            return Collections.emptyList();
        }
        return entityDao.getExecutedQueries();
    }
}
