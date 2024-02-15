package io.github.blyznytsiaorg.bibernate.dao;

import static io.github.blyznytsiaorg.bibernate.transaction.TransactionJdbcUtils.close;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.columnIdName;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.columnIdValue;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.columnName;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.columnVersionName;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.columnVersionValue;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getFieldValue;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getIdField;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getValueFromObject;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.isColumnHasAnnotation;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.isColumnVersionFound;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.isDynamicUpdate;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.joinColumnName;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.setVersionValueIfNull;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.table;
import static io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils.bidirectionalRelations;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_EXECUTE_DELETE_ENTITY_CLASS;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_EXECUTE_DELETE_ENTITY_CLASS_ALL_BY_ID;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_EXECUTE_FIND_BY_ENTITY_CLASS;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_EXECUTE_QUERY;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_EXECUTE_UPDATE_ENTITY_CLASS;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.COLLECTION_MUST_BE_NOT_EMPTY;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.ENTITY_CLASS_MUST_BE_NOT_NULL;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.ENTITY_MUST_BE_NOT_NULL;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.ENTITY_WAS_CHANGE_NEED_TO_GET_NEW_DATA;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.FIELD_MUST_BE_NOT_NULL;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.NON_UNIQUE_RESULT_FOR_FIND_BY_ID;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.PRIMARY_KEY_MUST_BE_NOT_NULL;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.DELETE;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.DELETE_ALL;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.QUERY;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.QUERY_BIND_TWO_VALUES;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.QUERY_BIND_VALUE;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.QUERY_BIND_VALUES;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.SAVE;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.SAVE_ALL;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.UPDATE;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class EntityDao implements Dao {

    private final SqlBuilder sqlBuilder;
    private final BibernateDatabaseSettings bibernateDatabaseSettings;
    private final EntityPersistent entityPersistent = new EntityPersistent();
    private final Identity identity;

    @Getter
    private final List<String> executedQueries;

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

    @Override
    public <T> List<T> findAllById(Class<T> entityClass, Collection<Object> primaryKeys) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        CollectionUtils.requireNonEmpty(primaryKeys, COLLECTION_MUST_BE_NOT_EMPTY);

        var tableName = table(entityClass);
        var fieldIdName = columnIdName(entityClass);
        var query = sqlBuilder.selectAllById(tableName, fieldIdName, primaryKeys.size());
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

    @Override
    public <T> List<T> findAllByColumnValue(Class<T> entityClass, String columnName, Object columnValue) {
        var whereCondition = sqlBuilder.fieldEqualsParameterCondition(columnName);

        return findByWhere(entityClass, whereCondition, columnValue);
    }

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

    @Override
    public <T> List<T> findByJoinTableField(Class<T> entityClass, Field field, Object... bindValues) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(field, FIELD_MUST_BE_NOT_NULL);

        var tableName = table(entityClass);
        var fieldIdName = columnIdName(entityClass);
        var query = sqlBuilder.selectWithJoin(tableName, fieldIdName, field);
        
        EntityMetadata entityMetadata = BibernateContextHolder.getBibernateEntityMetadata().get(entityClass);
        if (hasAnyOneToOneEagerFetchType(entityMetadata)) {
            return findAllByWhereJoin(entityClass, query, bindValues);
        }

        Optional.of(bidirectionalRelations(entityClass, field)).ifPresent(entityPersistent::addIgnoredRelationFields);

        var entities = findByQuery(entityClass, query, bindValues);

        entityPersistent.clearIgnoredRelationFields();

        return entities;
    }

    @Override
    public <T> List<T> findByWhereJoin(Class<T> entityClass,
                                       Object... bindValues) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);

        var dataSource = bibernateDatabaseSettings.getDataSource();

        String query = createLeftJoinQuery(entityClass);

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

    @Override
    public <T> List<T> findAllByWhereJoin(Class<T> entityClass, String query, Object... bindValues) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);

        var dataSource = bibernateDatabaseSettings.getDataSource();

        String leftJoinQuery = createLeftJoinQuery(entityClass);
        String mergedQuery = sqlBuilder.mergeQueries(leftJoinQuery, query);

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

    private <T> String createLeftJoinQuery(Class<T> entityClass) {
        var bibernateEntityMetadata = BibernateContextHolder.getBibernateEntityMetadata();
        var searchedEntityMetadata = bibernateEntityMetadata.get(entityClass);

        var tableName = searchedEntityMetadata.getTableName();
        var columnIdName = searchedEntityMetadata.getEntityColumns().stream()
                .filter(entityColumnDetails -> Objects.nonNull(entityColumnDetails.getId()))
                .map(EntityColumnDetails::getColumn)
                .map(ColumnMetadata::getName)
                .findFirst()
                .orElseThrow(() -> new BibernateGeneralException("Not specified entity Id"));

        var whereConditionId = tableName.concat(".").concat(columnIdName);
        var joinInfos = searchedEntityMetadata.joinInfos(
                entityClass, searchedEntityMetadata.getEntityColumns(), bibernateEntityMetadata, new HashSet<>()
        );

        return sqlBuilder.selectByWithJoin(tableName, whereConditionId, joinInfos, JoinType.LEFT);
    }

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

    @Override
    public <T> T save(Class<T> entityClass, T entity) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL);

        setVersionValueIfNull(entityClass, entity);

        identity.saveWithIdentity(entityClass, Collections.singletonList(entity));
        log.trace(SAVE, entityClass.getSimpleName());
        return entityClass.cast(entity);
    }

    @Override
    public <T> void saveAll(Class<T> entityClass, Collection<T> entities) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        CollectionUtils.requireNonEmpty(entities, COLLECTION_MUST_BE_NOT_EMPTY);

        setVersionValueIfNull(entityClass, entities);

        identity.saveWithIdentity(entityClass, entities);
        log.trace(SAVE_ALL, entityClass.getSimpleName());
    }

    @Override
    public <T> void deleteById(Class<T> entityClass, Object primaryKey) {
        Objects.requireNonNull(primaryKey, PRIMARY_KEY_MUST_BE_NOT_NULL);

        deleteByColumnValue(entityClass, columnIdName(entityClass), primaryKey, false);
    }

    @Override
    public <T> List<T> deleteByColumnValue(Class<T> entityClass, String columnName, Object value) {
        return deleteByColumnValue(entityClass, columnName, value, true);
    }

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

    @Override
    public void startTransaction() throws SQLException {
        getTransaction().start();
    }

    @Override
    public void commitTransaction() throws SQLException {
        var transaction = TransactionHolder.getTransaction();
        if (transaction != null) {
            transaction.commit();
            TransactionHolder.removeTransaction();
        }
    }

    @Override
    public void rollbackTransaction() throws SQLException {
        var transaction = TransactionHolder.getTransaction();
        if (transaction != null) {
            transaction.rollback();
            TransactionHolder.removeTransaction();
        }
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
