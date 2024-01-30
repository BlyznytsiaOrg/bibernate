package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.dao.exception.EntityStateWasChangeException;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder;
import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;
import io.github.blyznytsiaorg.bibernate.entity.EntityPersistent;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.exception.NonUniqueResultException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.*;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.*;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class EntityDao implements Dao {

    private final SqlBuilder sqlBuilder;
    private final BibernateDatabaseSettings bibernateDatabaseSettings;
    private final EntityPersistent entityPersistent = new EntityPersistent();
    @Getter
    private final List<String> executedQueries = new ArrayList<>();

    @Override
    public <T> Optional<T> findById(Class<T> entityClass, Object primaryKey) {
        Objects.requireNonNull(primaryKey, PRIMARY_KEY_MUST_BE_NOT_NULL);

        var fieldIdName = columnIdName(entityClass);

        List<T> resultList = findAllById(entityClass, fieldIdName, primaryKey);

        if (resultList.size() > 1) {
            throw new NonUniqueResultException(NON_UNIQUE_RESULT_FOR_FIND_BY_ID.formatted(entityClass.getSimpleName()));
        }

        return resultList.stream().findFirst();
    }

    @Override
    public <T> List<T> findAllById(Class<T> entityClass, String idColumnName, Object idColumnValue) {
        var whereCondition = sqlBuilder.selectByIdWhereCondition(idColumnName);

        return this.findByWhere(entityClass, whereCondition, idColumnValue);
    }

    @Override
    public <T> List<T> findByWhere(Class<T> entityClass, String whereCondition, Object... bindValues) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);

        var tableName = table(entityClass);
        var query = sqlBuilder.selectBy(tableName, whereCondition);

        return this.findByQuery(entityClass, query, bindValues);
    }

    @Override
    public <T> List<T> findByQuery(Class<T> entityClass, String query, Object... bindValues) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);

        var dataSource = bibernateDatabaseSettings.getDataSource();
        addToExecutedQueries(query);

        List<T> items = new ArrayList<>();
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            showSql(() -> log.debug(QUERY_BIND_VALUES, query, Arrays.toString(bindValues)));

            populatePreparedStatement(bindValues, statement);

            var resultSet = statement.executeQuery();
            while (resultSet.next()) {
                items.add(entityClass.cast(this.entityPersistent.toEntity(resultSet, entityClass)));
            }
        } catch (Exception exe) {
            String errorMessage = CANNOT_EXECUTE_FIND_BY_ENTITY_CLASS.formatted(entityClass, exe.getMessage());
            throwErrorMessage(errorMessage, exe);
        }

        return items;
    }

    @Override
    public int find(String query, Object[] bindValues) {
        var dataSource = bibernateDatabaseSettings.getDataSource();
        addToExecutedQueries(query);

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            showSql(() -> log.debug(QUERY_BIND_VALUES, query, Arrays.toString(bindValues)));

            populatePreparedStatement(bindValues, statement);

            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (Exception exe) {
            String errorMessage = CANNOT_EXECUTE_QUERY.formatted(query, exe.getMessage());
            throwErrorMessage(errorMessage, exe);
        }

        return 0;
    }

    @Override
    public <T> int update(Class<T> entityClass, Object entity, List<ColumnSnapshot> diff) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL);

        var dataSource = bibernateDatabaseSettings.getDataSource();

        var tableName = table(entityClass);
        var fieldIdName = columnIdName(entityClass);
        var fieldIdValue = columnIdValue(entityClass, entity);
        boolean isVersionFound = isColumnVersionFound(entityClass);
        Number fieldVersionValue = null;

        if (isVersionFound) {
            fieldVersionValue = (Number) columnVersionValue(entityClass, entity);
        }

        String query = sqlBuilder.update(entity, tableName, fieldIdName, diff);
        addToExecutedQueries(query);

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            showSql(() -> log.debug(QUERY, query));

            populatePreparedStatement(entity, statement, fieldIdName, fieldIdValue, fieldVersionValue, diff);
            var resultSet = statement.executeUpdate();
            log.trace(UPDATE, resultSet, entityClass.getSimpleName(), fieldIdValue);

            if (isVersionFound && resultSet == 0) {
                throw new EntityStateWasChangeException(
                        ENTITY_WAS_CHANGE_NEED_TO_GET_NEW_DATA
                                .formatted(entity.getClass(), fieldIdName, fieldIdValue)
                );
            }

            return resultSet;
        } catch (Exception exe) {
            String errorMessage = CANNOT_EXECUTE_UPDATE_ENTITY_CLASS
                    .formatted(entityClass, fieldIdValue, exe.getMessage());
            throwErrorMessage(errorMessage, exe);
        }

        return 0;
    }

    @Override
    public <T> T save(Class<T> entityClass, Object entity) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL);

        var dataSource = bibernateDatabaseSettings.getDataSource();

        var tableName = table(entityClass);
        var query = sqlBuilder.insert(entity, tableName);
        addToExecutedQueries(query);

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            showSql(() -> log.debug(QUERY, query));

            populatePreparedStatement(entity, statement);

            // TODO set id to Entity
            // TODO set to cash
            statement.execute();
            log.trace(SAVE, entityClass.getSimpleName());
        } catch (Exception exe) {
            String errorMessage = CANNOT_EXECUTE_SAVE_ENTITY_CLASS.formatted(entityClass, exe.getMessage());
            throwErrorMessage(errorMessage, exe);
        }

        return entityClass.cast(entity);
    }

    @Override
    public <T> void deleteById(Class<T> entityClass, Object primaryKey) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(primaryKey, PRIMARY_KEY_MUST_BE_NOT_NULL);

        var dataSource = bibernateDatabaseSettings.getDataSource();

        var tableName = table(entityClass);
        var fieldIdName = columnIdName(entityClass);
        var query = sqlBuilder.delete(tableName, fieldIdName);
        addToExecutedQueries(query);

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            showSql(() -> log.debug(QUERY_BIND_VALUE, query, fieldIdName, primaryKey));

            statement.setObject(1, primaryKey);

            statement.execute();
            log.trace(DELETE, entityClass.getSimpleName(), primaryKey);
        } catch (Exception exe) {
            String errorMessage = CANNOT_EXECUTE_DELETE_ENTITY_CLASS
                    .formatted(entityClass, primaryKey, exe.getMessage());
            throwErrorMessage(errorMessage, exe);
        }
    }

    @Override
    public <T> void delete(Class<T> entityClass, Object entity) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL);

        var fieldIdValue = columnIdValue(entityClass, entity);

        deleteById(entityClass, fieldIdValue);
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

    private void populatePreparedStatement(Object entity, PreparedStatement statement) throws SQLException {
        int index = 1;
        for (Field field : getInsertEntityFields(entity)) {
            statement.setObject(index++, getValueFromObject(entity, field));
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
                if (!isIdField(fieldIdName, field) && !isColumnWithVersion(field)) {
                    var fieldValue = getValueFromObject(entity, field);
                    statement.setObject(parameterIndex++, fieldValue);
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

    private void throwErrorMessage(String errorMessage, Exception exe) {
        log.error(errorMessage);
        throw new BibernateGeneralException(errorMessage, exe);
    }
}
