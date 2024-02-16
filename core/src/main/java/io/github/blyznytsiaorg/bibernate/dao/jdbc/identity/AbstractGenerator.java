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
 * An abstract base class for identity generators in Bibernate, providing common functionality
 * which can be used for generating insert queries to database.
 * <p>
 * This class is designed to be extended by specific generators that handle the id generation
 * and generations of insert queries</p>
 *
 */
@Slf4j
public abstract class AbstractGenerator {

    private final BibernateDatabaseSettings bibernateDatabaseSettings;
    private final List<String> executedQueries;


    /**
     * Constructs an AbstractGenerator with the specified Bibernate database settings
     * and a list to store executed queries.
     *
     * @param bibernateDatabaseSettings The database settings for Bibernate.
     * @param executedQueries           The list to store executed queries.
     */
    protected AbstractGenerator(BibernateDatabaseSettings bibernateDatabaseSettings,
                                List<String> executedQueries) {
        this.bibernateDatabaseSettings = bibernateDatabaseSettings;
        this.executedQueries = executedQueries;
    }

    /**
     * Adds a query to the list of executed queries if query collection is enabled.
     *
     * @param query The SQL query to be added to the executed queries list.
     */
    protected void addToExecutedQueries(String query) {
        if (bibernateDatabaseSettings.isCollectQueries()) {
            executedQueries.add(query);
        }
    }

    /**
     * Conditionally logins a provided SQL query based on the 'showSql' setting.
     *
     * @param logSql The SQL logging action to be executed.
     */
    protected void showSql(Runnable logSql) {
        if (bibernateDatabaseSettings.isShowSql()) {
            logSql.run();
        }
    }

    /**
     * Retrieves the batch size from the Bibernate database settings.
     *
     * @return The batch size for database queries.
     */
    protected Integer getBatchSize() {
        return bibernateDatabaseSettings.getBatchSize();
    }

    /**
     * Populates a prepared statement with values from the given entity, including the generated ID.
     *
     * @param entity       The entity for which the prepared statement is being populated.
     * @param statement    The prepared statement to be populated.
     * @param generatedId  The generated ID for the entity.
     * @throws SQLException If a SQL error occurs.
     */
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

    /**
     * Populates a prepared statement with values from the given entity.
     *
     * @param entity    The entity for which the prepared statement is being populated.
     * @param statement The prepared statement to be populated.
     * @throws SQLException If a SQL error occurs.
     */
    protected void populatePreparedStatement(Object entity, PreparedStatement statement) throws SQLException {
        int index = 1;
        for (Field field : getInsertEntityFields(entity.getClass())) {
            statement.setObject(index++, getValueFromObject(entity, field));
        }
    }

    /**
     * Populates a prepared statement with a list of values.
     *
     * @param values    The list of values to be used in the prepared statement.
     * @param statement The prepared statement to be populated.
     * @throws SQLException If a SQL error occurs.
     */
    protected void populatePreparedStatement(List<Object> values, PreparedStatement statement) throws SQLException {
        int index = 1;
        for (var value : values) {
            statement.setObject(index++, value);
        }
    }

    /**
     * Retrieves the generated value for a field, handling the case where it matches the generated ID.
     *
     * @param field             The field for which the generated value is retrieved.
     * @param generatedValueField The field representing the generated value.
     * @param generatedId       The generated ID.
     * @return An optional containing the generated value or empty if it is not the generated ID.
     */
    protected static Optional<Object> getGeneratedValue(Field field, Field generatedValueField, Object generatedId) {
        return field.equals(generatedValueField) ? Optional.ofNullable(generatedId) : Optional.empty();
    }

    /**
     * Adds an updated entity to the current transaction to be able to clean up ids in case of
     * rollback transaction.
     *
     * @param entity The entity to be added to the transaction.
     */
    protected void addUpdatedEntity(Object entity) {
        var transaction = TransactionHolder.getTransaction();
        if (transaction != null) {
            transaction.addUpdatedEntity(entity);
        }
    }

    /**
     * Generates and executes insert query for join table in case of many-to-many relation.
     *
     * @param entities   The collection of entities participating in the many-to-many relationship.
     * @param dataSource The data source for obtaining a database connection.
     * @param <T>        The type of entities in the collection.
     * @throws BibernateGeneralException If an error occurs while executing the SQL statements.
     */
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
