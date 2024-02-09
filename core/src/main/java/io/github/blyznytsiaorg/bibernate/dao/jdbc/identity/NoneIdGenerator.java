package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import io.github.blyznytsiaorg.bibernate.annotation.GenerationType;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;

import static io.github.blyznytsiaorg.bibernate.annotation.GenerationType.NONE;
import static io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder.insert;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.table;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_EXECUTE_SAVE_ENTITY_CLASS;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.QUERY;

/**
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Slf4j
public class NoneIdGenerator extends AbstractGenerator implements Generator {

    public NoneIdGenerator(
            BibernateDatabaseSettings bibernateDatabaseSettings,
            List<String> executedQueries) {
        super(bibernateDatabaseSettings, executedQueries);
    }

    @Override
    public GenerationType type() {
        return NONE;
    }

    @Override
    public <T> void handle(Class<T> entityClass, Collection<T> entities, DataSource dataSource) {
        var tableName = table(entityClass);
        var query = insert(entityClass, tableName);
        var entityArr = entities.toArray();
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(query);
            for (int i = 0; i < entityArr.length; i++) {
                populatePreparedStatement(entityArr[i], statement);
                ps.addBatch();
                addToExecutedQueries(query);

                if (i % getBatchSize() == 0 && i != 0) {
                    ps.executeBatch();
                }
            }

            ps.executeBatch();
            showSql(() -> log.debug(QUERY, query));
        } catch (Exception e) {
            throw new BibernateGeneralException(
                    CANNOT_EXECUTE_SAVE_ENTITY_CLASS.formatted(entityClass, e.getMessage()), e);
        } finally {
          close(connection, ps);
        }
    }
}
