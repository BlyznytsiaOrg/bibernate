package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.transaction.TransactionHolder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder.insert;
import static io.github.blyznytsiaorg.bibernate.transaction.TransactionJdbcUtils.close;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getIdValueFromField;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_EXECUTE_SAVE_ENTITY_CLASS;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.QUERY;

/**
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Slf4j
public abstract class AbstractGenerator {

    private final BibernateDatabaseSettings bibernateDatabaseSettings;
    private final List<String> executedQueries;

    protected AbstractGenerator(BibernateDatabaseSettings bibernateDatabaseSettings,
                                List<String> executedQueries) {
        this.bibernateDatabaseSettings = bibernateDatabaseSettings;
        this.executedQueries = executedQueries;
    }

    protected void addToExecutedQueries(String query) {
        if (bibernateDatabaseSettings.isCollectQueries()) {
            executedQueries.add(query);
        }
    }

    protected void showSql(Runnable logSql) {
        if (bibernateDatabaseSettings.isShowSql()) {
            logSql.run();
        }
    }

    protected Integer getBatchSize() {
        return bibernateDatabaseSettings.getBatchSize();
    }

    protected void populatePreparedStatement(Object entity, PreparedStatement statement, Object generatedId) throws SQLException {
        int index = 1;
        Field generatedValueField = getGeneratedValueField(entity);
        for (Field field : getInsertEntityFields(entity.getClass())) {
            statement.setObject(
                    index++,
                    getGeneratedValue(field, generatedValueField, generatedId)
                            .orElse(getValueFromObject(entity, field))
            );
        }
    }

    protected void populatePreparedStatement(Object entity, PreparedStatement statement) throws SQLException {
        int index = 1;
        for (Field field : getInsertEntityFields(entity.getClass())) {
            statement.setObject(index++, getValueFromObject(entity, field));
        }
    }

    protected void populatePreparedStatement(List<Object> values, PreparedStatement statement) throws SQLException {
        int index = 1;
        for (var value : values) {
            statement.setObject(index++, value);
        }
    }

    protected static Optional<Object> getGeneratedValue(Field field, Field generatedValueField, Object generatedId) {
        return field.equals(generatedValueField) ? Optional.ofNullable(generatedId) : Optional.empty();
    }

    protected void addUpdatedEntity(Object entity) {
        var transaction = TransactionHolder.getTransaction();
        if (transaction != null) {
            transaction.addUpdatedEntity(entity);
        }
    }

    @SneakyThrows
    protected <T> void insertManyToManyJoinTable(Collection<T> entities, DataSource dataSource) {
        for (var entity : entities) {
            for (var field : getManyToManyWithJoinTableFields(entity.getClass())){
                var entityId = getIdValueFromField(entity);
                var joinTableName = joinTableNameCorrect(field, entity.getClass());
                var joinColumn = tableJoinColumnNameCorrect(field, entity.getClass());
                var inverseJoinColumn = inverseTableJoinColumnName(field);
                field.setAccessible(true);
                var values = field.get(entity);
                var query = insert(joinTableName, List.of(joinColumn,inverseJoinColumn));
                if (values instanceof Collection<?>) {
                    showSql(() -> log.debug(QUERY, query));
                    Connection connection = null;
                    PreparedStatement ps = null;
                    try {
                        connection = dataSource.getConnection();
                        ps = connection.prepareStatement(query);
                        int count = 0;
                        for (var value : (Collection<?>)values) {
                            var inverseId = getIdValueFromField(value);
                            populatePreparedStatement(List.of(entityId, inverseId), ps);
                            ps.addBatch();
                            addToExecutedQueries(query);
                            if (++count % getBatchSize() == 0 && count != 0) {
                                ps.executeBatch();
                            }
                        }
                        ps.executeBatch();
                    } catch (Exception e) {
                        throw new BibernateGeneralException(
                                CANNOT_EXECUTE_SAVE_ENTITY_CLASS.formatted(entity.getClass(), e.getMessage()), e);
                    } finally {
                        close(connection, ps);
                    }
                }
            }
        }
    }
}
