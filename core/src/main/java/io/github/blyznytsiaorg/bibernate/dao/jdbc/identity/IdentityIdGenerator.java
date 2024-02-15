package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import io.github.blyznytsiaorg.bibernate.annotation.GenerationType;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;

import static io.github.blyznytsiaorg.bibernate.annotation.GenerationType.IDENTITY;
import static io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder.insert;
import static io.github.blyznytsiaorg.bibernate.transaction.TransactionJdbcUtils.close;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_EXECUTE_SAVE_ENTITY_CLASS;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.QUERY;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Slf4j
public class IdentityIdGenerator extends AbstractGenerator implements Generator {

    public IdentityIdGenerator(
            BibernateDatabaseSettings bibernateDatabaseSettings,
            List<String> executedQueries) {
        super(bibernateDatabaseSettings, executedQueries);
    }

    @Override
    public GenerationType type() {
        return IDENTITY;
    }

    @Override
    public <T> void handle(Class<T> entityClass, Collection<T> entities, DataSource dataSource) {
        var tableName = table(entityClass);
        var query = insert(entityClass, tableName);
        var entityArr = entities.toArray();

        showSql(() -> log.debug(QUERY, query));

        var countEntity = 0;
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(query, RETURN_GENERATED_KEYS);
            for (int i = 0; i < entityArr.length; i++) {
                populatePreparedStatement(entityArr[i], ps);
                ps.addBatch();
                addToExecutedQueries(query);

                if (i % getBatchSize() == 0 && i != 0) {
                    ps.executeBatch();
                    var generatedKeys = ps.getGeneratedKeys();
                    while (generatedKeys.next()) {
                        updateEntity(entityArr[countEntity++], generatedKeys.getObject(1));
                    }
                }
            }
            ps.executeBatch();
            var generatedKeys = ps.getGeneratedKeys();
            while (generatedKeys.next()) {
                updateEntity(entityArr[countEntity++], generatedKeys.getObject(1));
            }
            insertManyToManyJoinTable(entities, dataSource);
        } catch (Exception e) {
            throw new BibernateGeneralException(
                    CANNOT_EXECUTE_SAVE_ENTITY_CLASS.formatted(entityClass, e.getMessage()), e);
        } finally {
            close(connection, ps);
        }
    }

    private void updateEntity(Object entity, Object value) {
        setIdField(entity, value);
        addUpdatedEntity(entity);
    }
}
