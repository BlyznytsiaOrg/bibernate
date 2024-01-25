package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder;
import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;
import io.github.blyznytsiaorg.bibernate.entity.EntityPersistent;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class EntityDao implements Dao {

    private static final String CANNOT_EXECUTE_UPDATE_FOR_ENTITY_MESSAGE =
      "Cannot execute update of entityClass [%s] with id [%s], message [%s]";

    private static final String CANNOT_EXECUTE_FIND_BY_ENTITY_CLASS =
            "Cannot execute findById entityClass [%s], message: %s";

    private final SqlBuilder sqlBuilder;
    private final BibernateDatabaseSettings bibernateDatabaseSettings;
    private final EntityPersistent entityPersistent = new EntityPersistent();
    @Getter
    private final List<String> executedQueries = new ArrayList<>();

    @Override
    public <T> Optional<T> findById(Class<T> entityClass, Object primaryKey) {
        Objects.requireNonNull(primaryKey, "PrimaryKey must be not null");

        var fieldIdName = columnIdName(entityClass);
        
        return findAllById(entityClass, fieldIdName, primaryKey)
                .stream()
                .findFirst();
    }

    @Override
    public <T> List<T> findAllById(Class<T> entityClass, String idColumnName, Object idColumnValue) {
        var whereCondition = sqlBuilder.selectByIdWhereCondition(idColumnName);
        
        return findBy(entityClass, whereCondition, idColumnValue);
    }
    
    @Override
    public <T> List<T> findBy(Class<T> entityClass, String whereCondition, Object... bindValues) {
        Objects.requireNonNull(entityClass, "EntityClass must be not null");
        Objects.requireNonNull(whereCondition, "whereCondition must be not null");

        var tableName = table(entityClass);
        var dataSource = bibernateDatabaseSettings.getDataSource();

        var query = sqlBuilder.selectBy(tableName, whereCondition);
        if (bibernateDatabaseSettings.isCollectQueries()) {
            executedQueries.add(query);
        }

        List<T> items = new ArrayList<>();
        try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement(query)) {
            if (bibernateDatabaseSettings.isShowSql()) {
                log.info("Query {} bindValues {}", query, Arrays.toString(bindValues));
            }

            if (Objects.nonNull(bindValues)) {
                int index = 1;
                for (Object bindValue : bindValues) {
                    statement.setObject(index++, bindValue);
                }
            }

            var resultSet = statement.executeQuery();
            while (resultSet.next()) {
                items.add(entityClass.cast(this.entityPersistent.toEntity(resultSet, entityClass)));
            }
        } catch (Exception exe) {
            throw new BibernateGeneralException(
                    CANNOT_EXECUTE_FIND_BY_ENTITY_CLASS.formatted(entityClass, exe.getMessage()),
                    exe);
        }

        return items;
    }

    @Override
    public <T> T update(Class<T> entityClass, Object entity, List<ColumnSnapshot> diff) {
        Objects.requireNonNull(entityClass, "EntityClass must be not null");
        Objects.requireNonNull(entity, "Entity must be not null");

        var dataSource = bibernateDatabaseSettings.getDataSource();

        var tableName = table(entityClass);
        var fieldIdName = columnIdName(entityClass);
        var fieldIdValue = columnIdValue(entityClass, entity);

        String query = sqlBuilder.update(entity, tableName, fieldIdName, diff);
        if (bibernateDatabaseSettings.isCollectQueries()) {
            executedQueries.add(query);
        }

        try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement(query)) {
            if (bibernateDatabaseSettings.isShowSql()) {
                log.info("Query {}", query);
            }
            populatePreparedStatement(entity, statement, fieldIdName, fieldIdValue, diff);
            var resultSet = statement.executeUpdate();
            log.info("Update effected row {} for entity clazz {} with id {}",
                    resultSet, entityClass.getSimpleName(), fieldIdValue);
        } catch (SQLException exe) {
            throw new BibernateGeneralException(
              CANNOT_EXECUTE_UPDATE_FOR_ENTITY_MESSAGE.formatted(entityClass.getSimpleName(), fieldIdValue, exe.getMessage()), 
              exe);
        }

        return entityClass.cast(entity);
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
