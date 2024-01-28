package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.GenerationType;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.IdGenerator;
import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;
import io.github.blyznytsiaorg.bibernate.entity.EntityPersistent;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils;
import java.sql.ResultSet;
import java.sql.Statement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.GenerationType.*;
import static io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.IdGenerator.*;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;
import static java.sql.Statement.*;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class EntityDao implements Dao {

    private static final String CANNOT_EXECUTE_FIND_BY_ID_FOR_PRIMARY_KEY_MESSAGE =
            "Cannot execute findById entityClass [%s] for primaryKey %s message %s";
    private static final String CANNOT_EXECUTE_FIND_BY_ENTITY_CLASS_MESSAGE =
            "Cannot execute findById entityClass [%s] message %s";
    private static final String CANNOT_EXECUTE_UPDATE_ENTITY_CLASS_MESSAGE =
            "Cannot execute update entityClass [%s] for primaryKey %s message %s";
    private static final String CANNOT_EXECUTE_SAVE_ENTITY_CLASS_MESSAGE =
            "Cannot execute save entityClass [%s] message %s";
    private static final String CANNOT_EXECUTE_QUERY_MESSAGE = "Cannot execute query %s message %s";
    private static final String ENTITY_CLASS_MUST_BE_NOT_NULL_MESSAGE = "EntityClass must be not null";
    private static final String ENTITY_MUST_BE_NOT_NULL_MESSAGE = "Entity must be not null";
    private static final String PRIMARY_KEY_MUST_BE_NOT_NULL_MESSAGE = "PrimaryKey must be not null";

    private static final String QUERY_LOG = "Query {}";
    private static final String QUERY_BIND_VALUE_LOG = QUERY_LOG + " bindValue {}={}";
    private static final String QUERY_BIND_VALUES_LOG = QUERY_LOG + " bindValues {}";
    private static final String UPDATE_LOG = "Update effected row {} for entity clazz {} with id {}";
    private static final String SAVE_LOG = "Save entity clazz {}";

    private final SqlBuilder sqlBuilder;
    private final BibernateDatabaseSettings bibernateDatabaseSettings;
    private final EntityPersistent entityPersistent = new EntityPersistent();
    @Getter
    private final List<String> executedQueries = new ArrayList<>();

    @Override
    public <T> Optional<T> findById(Class<T> entityClass, Object primaryKey) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL_MESSAGE);
        Objects.requireNonNull(primaryKey, PRIMARY_KEY_MUST_BE_NOT_NULL_MESSAGE);

        var tableName = table(entityClass);
        var fieldIdName = columnIdName(entityClass);
        var dataSource = bibernateDatabaseSettings.getDataSource();

        var query = sqlBuilder.selectById(tableName, fieldIdName);
        addToExecutedQueries(query);

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            showSql(() -> log.info(QUERY_BIND_VALUE_LOG, query, fieldIdName, primaryKey));

            statement.setObject(1, primaryKey);
            var resultSet = statement.executeQuery();

            return resultSet.next() ? Optional.of(this.entityPersistent.toEntity(resultSet, entityClass)) : Optional.empty();
        } catch (Exception exe) {
            throw new BibernateGeneralException(
                    CANNOT_EXECUTE_FIND_BY_ID_FOR_PRIMARY_KEY_MESSAGE.formatted(entityClass, primaryKey, exe.getMessage()),
                    exe);
        }
    }

    @Override
    public <T> List<T> findBy(Class<T> entityClass, String whereCondition, Object[] bindValues) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL_MESSAGE);

        var tableName = table(entityClass);
        var dataSource = bibernateDatabaseSettings.getDataSource();

        var query = sqlBuilder.selectBy(tableName, whereCondition);
        addToExecutedQueries(query);

        List<T> items = new ArrayList<>();
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            showSql(() -> log.info(QUERY_BIND_VALUES_LOG, query, Arrays.toString(bindValues)));

            populatePreparedStatement(bindValues, statement);

            var resultSet = statement.executeQuery();
            while (resultSet.next()) {
                items.add(entityClass.cast(this.entityPersistent.toEntity(resultSet, entityClass)));
            }
        } catch (Exception exe) {
            throw new BibernateGeneralException(
                    CANNOT_EXECUTE_FIND_BY_ENTITY_CLASS_MESSAGE.formatted(entityClass, exe.getMessage()),
                    exe);
        }

        return items;
    }

    @Override
    public int find(String query, Object[] bindValues) {
        var dataSource = bibernateDatabaseSettings.getDataSource();
        addToExecutedQueries(query);

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            showSql(() -> log.info(QUERY_BIND_VALUES_LOG, query, Arrays.toString(bindValues)));

            populatePreparedStatement(bindValues, statement);

            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (Exception exe) {
            throw new BibernateGeneralException(
                    CANNOT_EXECUTE_QUERY_MESSAGE.formatted(query, exe.getMessage()),
                    exe);
        }

        return 0;
    }

    @Override
    public <T> T update(Class<T> entityClass, Object entity, List<ColumnSnapshot> diff) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL_MESSAGE);
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL_MESSAGE);

        var dataSource = bibernateDatabaseSettings.getDataSource();

        var tableName = table(entityClass);
        var fieldIdName = columnIdName(entityClass);
        var fieldIdValue = columnIdValue(entityClass, entity);

        String query = sqlBuilder.update(entity, tableName, fieldIdName, diff);
        addToExecutedQueries(query);

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            showSql(() -> log.info(QUERY_LOG, query));

            populatePreparedStatement(entity, statement, fieldIdName, fieldIdValue, diff);
            var resultSet = statement.executeUpdate();
            log.info(UPDATE_LOG, resultSet, entityClass.getSimpleName(), fieldIdValue);
        } catch (Exception exe) {
            throw new BibernateGeneralException(
                    CANNOT_EXECUTE_UPDATE_ENTITY_CLASS_MESSAGE.formatted(entityClass, fieldIdValue, exe.getMessage()),
                    exe);
        }

        return entityClass.cast(entity);
    }

    @Override
    public <T> T save(Class<T> entityClass, Object entity) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL_MESSAGE);
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL_MESSAGE);

        var dataSource = bibernateDatabaseSettings.getDataSource();

        var tableName = table(entityClass);
        var query = sqlBuilder.insert(entity, tableName);
        var idGenerator = createIdGenerator(entity, tableName, dataSource);
        idGenerator.getQueries().values().forEach(this::addToExecutedQueries);
        addToExecutedQueries(query);

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(query, RETURN_GENERATED_KEYS)) {

            showSql(() -> log.info(QUERY_LOG, query));

            if(SEQUENCE.equals(idGenerator.getStrategy())) {
                populatePreparedStatement(entity, statement, idGenerator.getGeneratedFields());

                statement.execute();
                Field idField = getIdField(entityClass);
                setField(idField, entity, idGenerator.getGeneratedFields().get(idField));
                log.info(SAVE_LOG, entityClass.getSimpleName());
            } else if (IDENTITY.equals(idGenerator.getStrategy())){
                populatePreparedStatement(entity, statement);
                statement.execute();
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    Field idField = getIdField(entityClass);
                    setField(idField, entity, generatedKeys.getObject(1));
                }
            } else {
                populatePreparedStatement(entity, statement);
                statement.execute();
            }
        } catch (Exception exe) {
            throw new BibernateGeneralException(
                    CANNOT_EXECUTE_SAVE_ENTITY_CLASS_MESSAGE.formatted(entityClass, exe.getMessage()),
                    exe);
        }

        return entityClass.cast(entity);
    }

    private <T> void setIdToEntity(Class<T> entityClass, Object entity, Object value) {
        Field idField = getIdField(entityClass);
        setField(idField, entity, value);
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

    private void populatePreparedStatement(Object entity, PreparedStatement statement, Map<Field, Object> generatedFields) throws SQLException {
        int index = 1;
        for (Field field : getInsertEntityFields(entity)) {
            statement.setObject(index++, getGeneratedValue(field, generatedFields).orElse(getValueFromObject(entity, field)));
        }
    }

    private Optional<Object> getGeneratedValue (Field field, Map<Field, Object> generatedFieldsMap) {
        return Optional.ofNullable(generatedFieldsMap.get(field));
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
                                           String fieldIdName, Object fieldIdValue,
                                           List<ColumnSnapshot> diff) throws SQLException {
        int parameterIndex = 1;

        if (isDynamicUpdate(entity.getClass())) {
            for (var columnSnapshot : diff) {
                statement.setObject(parameterIndex++, columnSnapshot.value());
            }

            statement.setObject(parameterIndex, fieldIdValue);
        } else {
            for (var field : entity.getClass().getDeclaredFields()) {
                if (!isIdField(fieldIdName, field)) {
                    var fieldValue = getValueFromObject(entity, field);
                    statement.setObject(parameterIndex++, fieldValue);
                }
            }

            statement.setObject(parameterIndex, fieldIdValue);
        }
    }

    private boolean isIdField(String fieldIdName, Field field) {
        return Objects.equals(fieldIdName, columnName(field));
    }
}
