package io.github.blyznytsiaorg.bibernate.dao.jdbc;


import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.DeleteQueryBuilder;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.InsertQueryBuilder;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.UpdateQueryBuilder;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.join.JoinType;
import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;
import io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils;

import java.util.Arrays;
import java.util.List;

import static io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.SelectQueryBuilder.*;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class SqlBuilder {

    public String selectBy(String tableName, String whereCondition) {
        return from(tableName)
                .whereCondition(whereCondition)
                .buildSelectStatement();
    }

    public String selectByWithJoin(String tableName, String whereCondition, String joinedTable, String onCondition, JoinType joinType) {
        return from(tableName)
                .join(joinedTable, onCondition, joinType)
                .whereCondition(whereCondition)
                .buildSelectStatement();
    }

    public String update(Object entity, String tableName, String fieldIdName, List<ColumnSnapshot> diff) {
        var entityClass = entity.getClass();
        var update = UpdateQueryBuilder.update(tableName);
        boolean isVersionFound = isColumnVersionFound(entityClass);
        String fieldVersionName = null;

        if (isVersionFound) {
            fieldVersionName = columnVersionName(entityClass);
        }

        final String finalFieldVersionName = fieldVersionName;
        if (!isDynamicUpdate(entityClass)) {
            var declaredFields = entityClass.getDeclaredFields();
            Arrays.stream(declaredFields)
                    .map(EntityReflectionUtils::columnName)
                    .filter(fieldName -> !fieldName.equals(fieldIdName))
                    .forEach(fieldName -> populateFieldOrIncVersion(fieldName, isVersionFound, finalFieldVersionName, update));
        } else {
            diff.stream()
                    .map(ColumnSnapshot::name)
                    .filter(fieldName -> !fieldName.equals(fieldIdName))
                    .forEach(fieldName -> populateFieldOrIncVersion(fieldName, isVersionFound, finalFieldVersionName, update));
        }

        var updateQueryBuilder = update.whereCondition(selectFieldNameWhereCondition(fieldIdName));
        if (isVersionFound) {
            updateQueryBuilder.andCondition(fieldVersionName + EQ + PARAMETER);
        }

        return updateQueryBuilder.buildUpdateStatement();
    }

    public String selectFieldNameWhereCondition(String fieldName) {
        return fieldName + EQ + PARAMETER;
    }
    

    public String insert(Object entity, String tableName) {
        var insert = InsertQueryBuilder.from(tableName);
        getInsertEntityFields(entity).forEach(field -> insert.setField(columnName(field)));

        return insert.buildInsertStatement();
    }

    public String delete(String tableName, String fieldIdName) {
        return DeleteQueryBuilder.from(tableName)
                .whereCondition(selectFieldNameWhereCondition(fieldIdName))
                .buildDeleteStatement();
    }

    public String delete(String tableName, String fieldIdName, String version) {
        return DeleteQueryBuilder.from(tableName)
                .whereCondition(selectFieldNameWhereCondition(fieldIdName))
                .andCondition(selectFieldNameWhereCondition(version))
                .buildDeleteStatement();
    }

    private void populateFieldOrIncVersion(String fieldName, boolean isVersionFound,
                                           String finalFieldVersionName, UpdateQueryBuilder update) {
        if (isVersionFound && finalFieldVersionName.equals(fieldName)) {
            update.setFieldIncrementVersion(fieldName);
        } else {
            update.setField(fieldName, PARAMETER);
        }
    }
}
