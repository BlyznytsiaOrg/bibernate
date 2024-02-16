package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.annotation.UpdateTimestamp;
import io.github.blyznytsiaorg.bibernate.annotation.Version;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.join.JoinType;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.identity.Identity;
import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;
import io.github.blyznytsiaorg.bibernate.entity.EntityPersistent;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityColumnDetails;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.ColumnMetadata;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.exception.EntityStateWasChangeException;
import io.github.blyznytsiaorg.bibernate.exception.NonUniqueResultException;
import io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder;
import io.github.blyznytsiaorg.bibernate.session.BibernateSession;
import io.github.blyznytsiaorg.bibernate.transaction.Transaction;
import io.github.blyznytsiaorg.bibernate.transaction.TransactionHolder;
import io.github.blyznytsiaorg.bibernate.utils.CollectionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.internal.util.Pair;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Predicate;

import static io.github.blyznytsiaorg.bibernate.dao.utils.HqlQueryInfo.DOT;
import static io.github.blyznytsiaorg.bibernate.transaction.TransactionJdbcUtils.close;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;
import static io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils.bidirectionalRelations;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.*;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.*;

/**
 * Data Access Object (DAO) implementation for managing entities in Bibernate.
 * <p>
 * This DAO provides methods for interacting with the database to perform CRUD operations on entities.
 * It uses a combination of JDBC and custom SQL queries to execute operations such as saving, updating, deleting,
 * and querying entities. The class is designed to be used with various entities and supports flexible querying.</p>
 *
 * <p>The DAO utilizes the provided {@link SqlBuilder} for constructing SQL queries,
 * {@link BibernateDatabaseSettings} for obtaining database-related settings,
 * {@link EntityPersistent} for entity-related persistence operations,
 * and {@link Identity} for managing identity generators.</p>
 *
 * <p>The class supports logging executed queries, which can be useful for debugging and monitoring database interactions.</p>
 *
 * <p>Note: This class assumes that entities follow the convention of having an identifier (ID) column,
 * and it supports primary key-based operations accordingly.</p>
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class EntityDao implements Dao {

    /**
     * The SQL builder used to construct SQL queries.
     */
    private final SqlBuilder sqlBuilder;

    /**
     * The settings related to the Bibernate database.
     */
    private final BibernateDatabaseSettings bibernateDatabaseSettings;

    /**
     * Manages the persistence state of entities during database operations.
     */
    private final EntityPersistent entityPersistent = new EntityPersistent();

    /**
     * The identity manager responsible for generating unique identifiers for entities.
     */
    private final Identity identity;

    /**
     * List of executed SQL queries during the session. Provides a record of queries executed.
     */
    @Getter
    private final List<String> executedQueries;

    /**
     * Retrieves an entity by its primary key. If the result set contains more than one entity,
     * a {@link NonUniqueResultException} is thrown.
     *
     * @param <T>          The type of the entity.
     * @param entityClass  The class of the entity.
     * @param primaryKey   The primary key value to search for.
     * @return An {@link Optional} containing the found entity, or an empty Optional if not found.
     * @throws NullPointerException if either {@code entityClass} or {@code primaryKey} is null.
     * @throws NonUniqueResultException if more than one entity is found for the given primary key.
     */
    @Override
    public <T> Optional<T> findById(Class<T> entityClass, Object primaryKey) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(primaryKey, PRIMARY_KEY_MUST_BE_NOT_NULL);

        var fieldIdName = columnIdName(entityClass);

        var resultList = findAllByColumnValue(entityClass, fieldIdName, primaryKey);

        if (resultList.size() > 1) {
            throw new NonUniqueResultException(NON_UNIQUE_RESULT_FOR_FIND_BY_ID.formatted(entityClass.getSimpleName()));
        }

        return resultList.stream().findFirst();
    }

    /**
     * Retrieves all entities of a given class from the database.
     *
     * @param <T>         The type of the entity.
     * @param entityClass The class of the entity.
     * @return A list containing all entities of the specified class.
     * @throws NullPointerException if {@code entityClass} is null.
     * @throws BibernateGeneralException if an error occurs while executing the query.
     */
    @Override
    public <T> List<T> findAll(Class<T> entityClass) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);

        var tableName = table(entityClass);
        var query = sqlBuilder.selectAll(tableName);
        var dataSource = bibernateDatabaseSettings.getDataSource();
        var items = new ArrayList<T>();

        addToExecutedQueries(query);
        showSql(() -> log.debug(QUERY, query));

        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(query);

            var resultSet = ps.executeQuery();
            while (resultSet.next()) {
                items.add(entityClass.cast(this.entityPersistent.toEntity(resultSet, entityClass)));
            }
        } catch (Exception exe) {
            var errorMessage = CANNOT_EXECUTE_FIND_BY_ENTITY_CLASS.formatted(entityClass, exe.getMessage());
            throwErrorMessage(errorMessage, exe);
        } finally {
            close(connection, ps);
        }

        return items;
    }

    /**
     * Retrieves entities by their primary keys from the database.
     *
     * @param <T>          The type of the entity.
     * @param entityClass  The class of the entity.
     * @param primaryKeys  The collection of primary key values to search for.
     * @return A list containing the found entities.
     * @throws NullPointerException if {@code entityClass} or {@code primaryKeys} is null.
     * @throws BibernateGeneralException if an error occurs while executing the query.
     */
    @Override
    public <T> List<T> findAllById(Class<T> entityClass, Collection<Object> primaryKeys) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        CollectionUtils.requireNonEmpty(primaryKeys, COLLECTION_MUST_BE_NOT_EMPTY);

        var tableName = table(entityClass);
        var fieldIdName = columnIdName(entityClass);
        var query = sqlBuilder.selectAllByFieldName(tableName, fieldIdName, primaryKeys.size());
        var dataSource = bibernateDatabaseSettings.getDataSource();
        var ids = primaryKeys.toArray();
        var items = new ArrayList<T>();

        addToExecutedQueries(query);
        showSql(() -> log.debug(QUERY_BIND_VALUES, query, primaryKeys));

        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(query);

            populatePreparedStatement(ids, ps);

            var resultSet = ps.executeQuery();
            while (resultSet.next()) {
                items.add(entityClass.cast(this.entityPersistent.toEntity(resultSet, entityClass)));
            }
        } catch (Exception exe) {
            var errorMessage = CANNOT_EXECUTE_FIND_BY_ENTITY_CLASS.formatted(entityClass, exe.getMessage());
            throwErrorMessage(errorMessage, exe);
        } finally {
            close(connection, ps);
        }

        return items;
    }

    /**
     * Retrieves a list of entities of type {@code T} based on the specified column's value.
     * <p>
     * This method constructs a WHERE condition using the provided column name and value
     * and delegates to {@link #findByWhere(Class, String, Object...)} method for execution.</p>
     *
     * @param <T>          The type of entities to retrieve.
     * @param entityClass  The class of the entity.
     * @param columnName   The name of the column.
     * @param columnValue  The value to match in the specified column.
     * @return A list of entities matching the specified column value.
     */
    @Override
    public <T> List<T> findAllByColumnValue(Class<T> entityClass, String columnName, Object columnValue) {
        var whereCondition = sqlBuilder.fieldEqualsParameterCondition(columnName);

        return findByWhere(entityClass, whereCondition, columnValue);
    }

    /**
     * Retrieves a list of entities of type {@code T} based on the specified WHERE condition.
     * <p>
     * This method constructs a SELECT query using the provided entity class, WHERE condition,
     * and optional bind values, and delegates to {@link #findByQuery(Class, String, Object...)} method for execution.</p>
     * <p>
     * If the entity has any one-to-one eager fetch type relationships, it utilizes a left join for fetching.</p>
     *
     * @param <T>            The type of entities to retrieve.
     * @param entityClass    The class of the entity.
     * @param whereCondition The WHERE condition for the query.
     * @param bindValues     The optional bind values for the WHERE condition.
     * @return A list of entities matching the specified WHERE condition.
     */
    @Override
    public <T> List<T> findByWhere(Class<T> entityClass, String whereCondition, Object... bindValues) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        EntityMetadata entityMetadata = BibernateContextHolder.getBibernateEntityMetadata().get(entityClass);

        if (hasAnyOneToOneEagerFetchType(entityMetadata)) {
            return findByWhereJoin(entityClass, bindValues);
        }
        var tableName = table(entityClass);
        var query = sqlBuilder.selectBy(tableName, whereCondition);

        return findByQuery(entityClass, query, bindValues);
    }

    /**
     * Retrieves a list of entities of type {@code T} based on the specified field and optional bind values.
     * <p>
     * This method constructs a SELECT query using the provided entity class, field, and optional bind values,
     * and delegates to {@link #findByQuery(Class, String, Object...)} method for execution.</p>
     * <p>
     * If the entity has any one-to-one eager fetch type relationships, it utilizes a left join for fetching.</p>
     *
     * @param <T>         The type of entities to retrieve.
     * @param entityClass The class of the entity.
     * @param field       The field for the join operation.
     * @param bindValues  The optional bind values for the join operation.
     * @return A list of entities matching the specified join table field.
     */
    @Override
    public <T> List<T> findByJoinTableField(Class<T> entityClass, Field field, Object... bindValues) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(field, FIELD_MUST_BE_NOT_NULL);

        var tableName = table(entityClass);
        var fieldIdName = columnIdName(entityClass);
        var query = sqlBuilder.selectWithJoin(tableName, fieldIdName, field);

        var entityMetadata = BibernateContextHolder.getBibernateEntityMetadata().get(entityClass);
        if (hasAnyOneToOneEagerFetchType(entityMetadata)) {
            return findAllByWhereJoin(entityClass, query, bindValues);
        }

        Optional.of(bidirectionalRelations(entityClass, field)).ifPresent(entityPersistent::addIgnoredRelationFields);

        var entities = findByQuery(entityClass, query, bindValues);

        entityPersistent.clearIgnoredRelationFields();

        return entities;
    }

    /**
     * Retrieves a list of entities of type {@code T} using a left join for fetching,
     * based on the specified entity class and optional bind values.
     *
     * @param <T>         The type of entities to retrieve.
     * @param entityClass The class of the entity.
     * @param bindValues  The optional bind values for the left join operation.
     * @return A list of entities matching the specified left join operation.
     */
    @Override
    public <T> List<T> findByWhereJoin(Class<T> entityClass,
                                       Object... bindValues) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);

        var dataSource = bibernateDatabaseSettings.getDataSource();

        var query = createLeftJoinQuery(entityClass);

        addToExecutedQueries(query);
        var items = new ArrayList<T>();

        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(query);

            showSql(() -> log.debug(QUERY_BIND_VALUES, query, Arrays.toString(bindValues)));

            populatePreparedStatement(bindValues, ps);

            var resultSet = ps.executeQuery();
            if (resultSet.next()) {
                items.add(entityClass.cast(this.entityPersistent.toEntity(resultSet, entityClass)));
            }
        } catch (Exception exe) {
            var errorMessage = CANNOT_EXECUTE_FIND_BY_ENTITY_CLASS.formatted(entityClass, exe.getMessage());
            throwErrorMessage(errorMessage, exe);
        } finally {
            close(connection, ps);
        }

        return items;
    }

    /**
     * Retrieves a list of entities of type {@code T} using a left join for fetching,
     * based on the specified entity class, query, and optional bind values.
     *
     * @param <T>         The type of entities to retrieve.
     * @param entityClass The class of the entity.
     * @param query       The query for the left join operation.
     * @param bindValues  The optional bind values for the left join operation.
     * @return A list of entities matching the specified left join operation.
     */
    @Override
    public <T> List<T> findAllByWhereJoin(Class<T> entityClass, String query, Object... bindValues) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);

        var dataSource = bibernateDatabaseSettings.getDataSource();

        var leftJoinQuery = createLeftJoinQuery(entityClass);
        var mergedQuery = sqlBuilder.mergeQueries(leftJoinQuery, query);

        addToExecutedQueries(mergedQuery);
        var items = new ArrayList<T>();

        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(mergedQuery);

            showSql(() -> log.debug(QUERY_BIND_VALUES, mergedQuery, Arrays.toString(bindValues)));

            populatePreparedStatement(bindValues, ps);

            var resultSet = ps.executeQuery();
            while (resultSet.next()) {
                items.add(entityClass.cast(this.entityPersistent.toEntity(resultSet, entityClass)));
            }
        } catch (Exception exe) {
            var errorMessage = CANNOT_EXECUTE_FIND_BY_ENTITY_CLASS.formatted(entityClass, exe.getMessage());
            throwErrorMessage(errorMessage, exe);
        } finally {
            close(connection, ps);
        }

        return items;
    }

    /**
     * Retrieves a list of entities of type {@code T} based on the provided SQL query and optional bind values.
     * <p>
     * This method prepares a SQL query, executes it, and maps the result set to entities of type {@code T}.</p>
     *
     * @param <T>          The type of entities to retrieve.
     * @param entityClass  The class of the entity.
     * @param query        The SQL query to execute.
     * @param bindValues   The optional bind values for the query.
     * @return A list of entities based on the specified SQL query.
     */
    @Override
    public <T> List<T> findByQuery(Class<T> entityClass, String query, Object... bindValues) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);

        var dataSource = bibernateDatabaseSettings.getDataSource();
        addToExecutedQueries(query);

        var items = new ArrayList<T>();
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(query);

            showSql(() -> log.debug(QUERY_BIND_VALUES, query, Arrays.toString(bindValues)));

            populatePreparedStatement(bindValues, ps);

            var resultSet = ps.executeQuery();
            while (resultSet.next()) {
                items.add(entityClass.cast(this.entityPersistent.toEntity(resultSet, entityClass)));
            }
        } catch (Exception exe) {
            var errorMessage = CANNOT_EXECUTE_FIND_BY_ENTITY_CLASS.formatted(entityClass, exe.getMessage());
            throwErrorMessage(errorMessage, exe);
        } finally {
            close(connection, ps);
        }

        return items;
    }

    /**
     * Executes a SQL query and retrieves an integer result.
     *
     * @param query      The SQL query to execute.
     * @param bindValues The bind values for the query.
     * @return The integer result obtained from the executed SQL query.
     */
    @Override
    public int find(String query, Object[] bindValues) {
        var dataSource = bibernateDatabaseSettings.getDataSource();
        addToExecutedQueries(query);
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(query);

            showSql(() -> log.debug(QUERY_BIND_VALUES, query, Arrays.toString(bindValues)));

            populatePreparedStatement(bindValues, ps);

            var resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (Exception exe) {
            var errorMessage = CANNOT_EXECUTE_QUERY.formatted(query, exe.getMessage());
            throwErrorMessage(errorMessage, exe);
        } finally {
            close(connection, ps);
        }

        return 0;
    }

    /**
     * Updates an entity of type {@code T} in the database based on the provided differences.
     * <p>
     * This method prepares and executes an SQL update query and handles version checking if a version column is present.</p>
     *
     * @param <T>        The type of entity to update.
     * @param entityClass The class of the entity.
     * @param entity     The entity to update.
     * @param diff       The list of differences between the current and original entity state.
     */
    @Override
    public <T> void update(Class<T> entityClass, Object entity, List<ColumnSnapshot> diff) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL);

        var dataSource = bibernateDatabaseSettings.getDataSource();

        var tableName = table(entityClass);
        var fieldIdName = columnIdName(entityClass);
        var fieldIdValue = columnIdValue(entityClass, entity);
        var isVersionFound = isColumnVersionFound(entityClass);
        Number fieldVersionValue = null;

        if (isVersionFound) {
            fieldVersionValue = (Number) columnVersionValue(entityClass, entity);
        }

        var query = sqlBuilder.update(entity, tableName, fieldIdName, diff);
        addToExecutedQueries(query);

        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(query);

            showSql(() -> log.debug(QUERY, query));

            populatePreparedStatement(entity, ps, fieldIdName, fieldIdValue, fieldVersionValue, diff);
            var resultSet = ps.executeUpdate();
            log.trace(UPDATE, resultSet, entityClass.getSimpleName(), fieldIdValue);

            if (isVersionFound && resultSet == 0) {
                throw new EntityStateWasChangeException(
                        ENTITY_WAS_CHANGE_NEED_TO_GET_NEW_DATA
                                .formatted(entity.getClass(), fieldIdName, fieldIdValue)
                );
            }
        } catch (Exception exe) {
            var errorMessage = CANNOT_EXECUTE_UPDATE_ENTITY_CLASS
                    .formatted(entityClass, fieldIdValue, exe.getMessage());
            throwErrorMessage(errorMessage, exe);
        } finally {
            close(connection, ps);
        }
    }

    /**
     * Saves an entity of type {@code T} in the database.
     * <p>
     * This method sets the version value if null, saves the entity using the identity,
     * generates sql query for insert using different id generators.</p>
     *
     * @param <T>        The type of entity to save.
     * @param entityClass The class of the entity.
     * @param entity     The entity to save.
     * @return The saved entity.
     */
    @Override
    public <T> T save(Class<T> entityClass, T entity) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL);

        setVersionValueIfNull(entityClass, entity);

        identity.saveWithIdentity(entityClass, Collections.singletonList(entity));
        log.trace(SAVE, entityClass.getSimpleName());
        return entityClass.cast(entity);
    }

    /**
     * Saves a collection of entities of type {@code T} in the database.
     * <p>
     * This method sets the version value if null, saves the collection using the identity,
     * generates sql query for insert using different id generators.</p>
     *
     * @param <T>        The type of entities to save.
     * @param entityClass The class of the entities.
     * @param entities    The collection of entities to save.
     */
    @Override
    public <T> void saveAll(Class<T> entityClass, Collection<T> entities) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        CollectionUtils.requireNonEmpty(entities, COLLECTION_MUST_BE_NOT_EMPTY);

        setVersionValueIfNull(entityClass, entities);

        identity.saveWithIdentity(entityClass, entities);
        log.trace(SAVE_ALL, entityClass.getSimpleName());
    }

    /**
     * Deletes an entity of type {@code T} by its primary key.
     *
     * @param <T>         The type of entity to delete.
     * @param entityClass  The class of the entity.
     * @param primaryKey   The primary key of the entity to delete.
     */
    @Override
    public <T> void deleteById(Class<T> entityClass, Object primaryKey) {
        Objects.requireNonNull(primaryKey, PRIMARY_KEY_MUST_BE_NOT_NULL);

        deleteByColumnValue(entityClass, columnIdName(entityClass), primaryKey, false);
    }

    /**
     * Deletes entities of type {@code T} based on a specified column value.
     *
     * @param <T>        The type of entities to delete.
     * @param entityClass The class of the entities.
     * @param columnName  The name of the column to use for deletion.
     * @param value       The value of the column to match for deletion.
     * @return A list of deleted entities.
     */
    @Override
    public <T> List<T> deleteByColumnValue(Class<T> entityClass, String columnName, Object value) {
        return deleteByColumnValue(entityClass, columnName, value, true);
    }

    /**
     * Deletes entities of type {@code T} by their primary keys.
     *
     * @param <T>          The type of entities to delete.
     * @param entityClass  The class of the entities.
     * @param primaryKeys  The collection of primary keys for entities to delete.
     */
    @Override
    public <T> void deleteAllById(Class<T> entityClass, Collection<Object> primaryKeys) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        CollectionUtils.requireNonEmpty(primaryKeys, COLLECTION_MUST_BE_NOT_EMPTY);

        var dataSource = bibernateDatabaseSettings.getDataSource();
        var tableName = table(entityClass);
        var fieldIdName = columnIdName(entityClass);
        var query = sqlBuilder.delete(tableName, fieldIdName);
        showSql(() -> log.debug(QUERY_BIND_VALUES, query, primaryKeys));

        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(query);

            var ids = primaryKeys.toArray();
            for (int i = 0; i < ids.length; i++) {
                if (i % bibernateDatabaseSettings.getBatchSize() == 0) {
                    ps.executeBatch();
                }
                ps.setObject(1, ids[i]);
                ps.addBatch();
                addToExecutedQueries(query);
            }
            ps.executeBatch();

            log.trace(DELETE_ALL, entityClass.getSimpleName(), primaryKeys, bibernateDatabaseSettings.getBatchSize());
        } catch (Exception exe) {
            var errorMessage = CANNOT_EXECUTE_DELETE_ENTITY_CLASS_ALL_BY_ID
                    .formatted(entityClass, primaryKeys, exe.getMessage());
            throwErrorMessage(errorMessage, exe);
        } finally {
            close(connection, ps);
        }
    }

    /**
     * Deletes an entity of type {@code T} by providing the entity instance.
     *
     * @param <T>     The type of entity to delete.
     * @param entityClass The class of the entity.
     * @param entity  The entity instance to delete.
     */
    @Override
    public <T> void delete(Class<T> entityClass, Object entity) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL);

        var primaryKey = columnIdValue(entityClass, entity);
        var isVersionFound = isColumnVersionFound(entityClass);
        Number fieldVersionValue;

        if (isVersionFound) {
            fieldVersionValue = (Number) columnVersionValue(entityClass, entity);
        } else {
            fieldVersionValue = null;
        }

        var dataSource = bibernateDatabaseSettings.getDataSource();

        var tableName = table(entityClass);
        var fieldIdName = columnIdName(entityClass);
        String query;

        if (isVersionFound) {
            String columnVersionName = columnVersionName(entityClass);
            query = sqlBuilder.delete(tableName, fieldIdName, columnVersionName);
            showSql(() -> log.debug(QUERY_BIND_TWO_VALUES, query, fieldIdName, primaryKey, columnVersionName, fieldVersionValue));
        } else {
            query = sqlBuilder.delete(tableName, fieldIdName);
            showSql(() -> log.debug(QUERY_BIND_VALUE, query, fieldIdName, primaryKey));
        }

        addToExecutedQueries(query);
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(query);

            ps.setObject(1, primaryKey);

            if (isVersionFound) {
                ps.setObject(2, fieldVersionValue);
            }

            ps.execute();
            log.trace(DELETE, entityClass.getSimpleName(), fieldIdName, primaryKey);
        } catch (Exception exe) {
            var errorMessage = CANNOT_EXECUTE_DELETE_ENTITY_CLASS
                    .formatted(entityClass, primaryKey, exe.getMessage());
            throwErrorMessage(errorMessage, exe);
        } finally {
            close(connection, ps);
        }
    }

    /**
     * Deletes all entities of type {@code T} from the database based on the provided collection of entities.
     * <p>
     * This method takes a collection of entities, determines their primary keys and version values (if applicable),
     * and executes batch delete queries to remove the entities from the database.</p>
     *
     * @param <T>        The type of entities to delete.
     * @param entityClass The class of the entities.
     * @param entities    The collection of entities to delete.
     */
    @Override
    public <T> void deleteAll(Class<T> entityClass, Collection<T> entities) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        CollectionUtils.requireNonEmpty(entities, COLLECTION_MUST_BE_NOT_EMPTY);

        var isVersionFound = isColumnVersionFound(entityClass);
        var dataSource = bibernateDatabaseSettings.getDataSource();
        var tableName = table(entityClass);
        var fieldIdName = columnIdName(entityClass);
        var primaryKeyToVersionValues = preparePrimaryKeyToVersionValues(entityClass, entities, isVersionFound);
        var primaryKeys = primaryKeyToVersionValues.stream().map(Pair::getLeft).toList();
        var query = prepareQuery(entityClass, isVersionFound, tableName, fieldIdName, primaryKeyToVersionValues, primaryKeys);

        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(query);

            for (int i = 0; i < primaryKeyToVersionValues.size(); i++) {
                if (i % bibernateDatabaseSettings.getBatchSize() == 0) {
                    ps.executeBatch();
                }
                ps.setObject(1, primaryKeyToVersionValues.get(i).getLeft());
                if (isVersionFound) {
                    ps.setObject(2, primaryKeyToVersionValues.get(i).getRight());
                }
                ps.addBatch();
                addToExecutedQueries(query);
            }
            ps.executeBatch();

            log.trace(DELETE_ALL, entityClass.getSimpleName(), primaryKeys, bibernateDatabaseSettings.getBatchSize());
        } catch (Exception exe) {
            var errorMessage = CANNOT_EXECUTE_DELETE_ENTITY_CLASS
                    .formatted(entityClass, primaryKeys, exe.getMessage());
            throwErrorMessage(errorMessage, exe);
        } finally {
            close(connection, ps);
        }
    }

    /**
     * Starts a new transaction and put it into ThreadLocal. If transaction is already in ThreadLocal
     * will be kept the same.
     *
     * @throws SQLException If an SQL exception occurs while starting the transaction.
     */
    @Override
    public void startTransaction() throws SQLException {
        getTransaction().start();
    }

    /**
     * Commits the current transaction.
     *
     * @throws SQLException If an SQL exception occurs while committing the transaction.
     */
    @Override
    public void commitTransaction() throws SQLException {
        var transaction = TransactionHolder.getTransaction();
        if (transaction != null) {
            transaction.commit();
            TransactionHolder.removeTransaction();
        }
    }

    /**
     * Rolls back the current transaction.
     *
     * @throws SQLException If an SQL exception occurs while rolling back the transaction.
     */
    @Override
    public void rollbackTransaction() throws SQLException {
        var transaction = TransactionHolder.getTransaction();
        if (transaction != null) {
            transaction.rollback();
            TransactionHolder.removeTransaction();
        }
    }

    private <T> String createLeftJoinQuery(Class<T> entityClass) {
        var bibernateEntityMetadata = BibernateContextHolder.getBibernateEntityMetadata();
        var searchedEntityMetadata = bibernateEntityMetadata.get(entityClass);

        var tableName = searchedEntityMetadata.getTableName();
        var columnIdName = searchedEntityMetadata.getEntityColumns().stream()
                .filter(entityColumnDetails -> Objects.nonNull(entityColumnDetails.getId()))
                .map(EntityColumnDetails::getColumn)
                .map(ColumnMetadata::getName)
                .findFirst()
                .orElseThrow(() -> new BibernateGeneralException(NOT_SPECIFIED_ENTITY_ID));

        var whereConditionId = tableName.concat(DOT).concat(columnIdName);
        var joinInfos = searchedEntityMetadata.joinInfos(
                entityClass, searchedEntityMetadata.getEntityColumns(), bibernateEntityMetadata, new HashSet<>()
        );

        return sqlBuilder.selectByWithJoin(tableName, whereConditionId, joinInfos, JoinType.LEFT);
    }

    private <T> List<T> deleteByColumnValue(Class<T> entityClass, String columnName, Object value,
                                            boolean returnDeletedEntities) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(columnName, FIELD_MUST_BE_NOT_NULL);

        var dataSource = bibernateDatabaseSettings.getDataSource();

        var bibernateEntityMetadata = BibernateContextHolder.getBibernateEntityMetadata();
        var entityMetadata = bibernateEntityMetadata.get(entityClass);
        var tableName = entityMetadata.getTableName();

        var session = BibernateContextHolder.getBibernateSession();
        var relationsForRemoval = entityMetadata.getCascadeRemoveRelations();

        List<T> deletedEntities = Collections.emptyList();
        if (returnDeletedEntities || CollectionUtils.isNotEmpty(relationsForRemoval)) {
            deletedEntities = findAllByColumnValue(entityClass, columnName, value);
        }

        removeToManyRelations(entityClass, value, session, relationsForRemoval);

        var query = sqlBuilder.delete(tableName, columnName);
        addToExecutedQueries(query);
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(query);

            showSql(() -> log.debug(QUERY_BIND_VALUE, query, columnName, value));

            ps.setObject(1, value);

            ps.execute();
        } catch (Exception exe) {
            log.error(CANNOT_EXECUTE_DELETE_ENTITY_CLASS.formatted(entityClass, value, exe.getMessage()));
            return Collections.emptyList();
        } finally {
            close(connection, ps);
        }

        removeToOneRelations(deletedEntities, session, relationsForRemoval);

        return deletedEntities;
    }

    private Transaction getTransaction() throws SQLException {
        var transaction = TransactionHolder.getTransaction();
        if (transaction == null) {
            var connection = bibernateDatabaseSettings.getDataSource().getConnection();
            transaction = new Transaction(connection);
            TransactionHolder.setTransaction(new Transaction(connection));
        }
        return transaction;
    }

    private <T> void removeToManyRelations(Class<T> entityClass, Object value, BibernateSession session,
                                           List<EntityColumnDetails> relationsForRemoval) {
        relationsForRemoval
                .stream()
                .filter(EntityColumnDetails::isCollection)
                .forEach(column -> {
                    var type = column.getFieldType();
                    var joinColumnName = Optional.ofNullable(joinColumnName(type, entityClass))
                            .orElse(joinColumnName(entityClass, type));
                    session.deleteByColumnValue(type, joinColumnName, value);
                });
    }

    private <T> void removeToOneRelations(List<T> deletedEntities, BibernateSession session,
                                          List<EntityColumnDetails> relationsForRemoval) {
        relationsForRemoval
                .stream()
                .filter(Predicate.not(EntityColumnDetails::isCollection))
                .forEach(column -> deletedEntities.forEach(deletedEntity -> {
                    var relatedEntity = getFieldValue(column.getField(), deletedEntity);
                    var relatedEntityId = getFieldValue(getIdField(relatedEntity.getClass()), relatedEntity);
                    session.deleteById(relatedEntity.getClass(), relatedEntityId);
                }));
    }

    private <T> List<Pair<Object, Object>> preparePrimaryKeyToVersionValues(Class<T> entityClass,
                                                                            Collection<T> entities,
                                                                            boolean isVersionFound) {
        return entities.stream()
                .map(entity -> {
                    var id = columnIdValue(entityClass, entity);
                    var version = isVersionFound ? columnVersionValue(entityClass, entity) : null;
                    return Pair.of(id, version);
                })
                .toList();
    }

    private <T> String prepareQuery(Class<T> entityClass,
                                    boolean isVersionFound,
                                    String tableName,
                                    String fieldIdName,
                                    List<Pair<Object, Object>> primaryKeyToVersionValues,
                                    List<Object> primaryKeys) {
        String query;
        if (isVersionFound) {
            var columnVersionName = columnVersionName(entityClass);
            var versions = primaryKeyToVersionValues.stream().map(Pair::getRight).toList();
            query = sqlBuilder.delete(tableName, fieldIdName, columnVersionName);
            showSql(() -> log.debug(QUERY_BIND_TWO_VALUES, query, fieldIdName, primaryKeys, columnVersionName, versions));
            return query;
        }

        query = sqlBuilder.delete(tableName, fieldIdName);
        showSql(() -> log.debug(QUERY_BIND_VALUES, query, primaryKeys));
        return query;
    }

    private void addToExecutedQueries(String query) {
        if (bibernateDatabaseSettings.isCollectQueries()) {
            executedQueries.add(query);
        }
    }

    private void showSql(Runnable logSql) {
        if (bibernateDatabaseSettings.isShowSql()) {
            logSql.run();
        }
    }

    private void populatePreparedStatement(Object[] bindValues, PreparedStatement statement) throws SQLException {
        if (Objects.nonNull(bindValues)) {
            int index = 1;
            for (Object bindValue : bindValues) {
                statement.setObject(index++, bindValue);
            }
        }
    }

    private void populatePreparedStatement(Object entity, PreparedStatement statement,
                                           String fieldIdName, Object fieldIdValue, Number fieldVersionValue,
                                           List<ColumnSnapshot> diff) throws SQLException {
        int parameterIndex = 1;

        if (isDynamicUpdate(entity.getClass())) {
            for (var columnSnapshot : diff) {
                statement.setObject(parameterIndex++, columnSnapshot.value());
            }
        } else {
            for (var field : entity.getClass().getDeclaredFields()) {
                if (!isIdField(fieldIdName, field) && !isColumnHasAnnotation(field, Version.class)) {
                    if (isUpdateTimestamp(field)) {
                        statement.setObject(parameterIndex++, OffsetDateTime.now());
                    } else {
                        var fieldValue = getValueFromObject(entity, field);
                        statement.setObject(parameterIndex++, fieldValue);
                    }
                }
            }
        }

        statement.setObject(parameterIndex++, fieldIdValue);

        if (Objects.nonNull(fieldVersionValue)) {
            statement.setObject(parameterIndex, fieldVersionValue);
        }
    }

    private boolean isIdField(String fieldIdName, Field field) {
        return Objects.equals(fieldIdName, columnName(field));
    }

    private boolean isUpdateTimestamp(Field field) {
        return field.isAnnotationPresent(UpdateTimestamp.class);
    }

    private void throwErrorMessage(String errorMessage, Exception exe) {
        log.error(errorMessage);
        throw new BibernateGeneralException(errorMessage, exe);
    }

    private boolean hasAnyOneToOneEagerFetchType(EntityMetadata entityMetadata) {
        return entityMetadata.getEntityColumns().stream()
                .map(EntityColumnDetails::getOneToOne)
                .filter(Objects::nonNull)
                .anyMatch(oneToOneMetadata -> oneToOneMetadata.getFetchType() == FetchType.EAGER);
    }
}
