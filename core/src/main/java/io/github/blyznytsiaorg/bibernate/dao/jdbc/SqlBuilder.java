package io.github.blyznytsiaorg.bibernate.dao.jdbc;


import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.InsertQueryBuilder;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.UpdateQueryBuilder;
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

    public String selectById(String tableName, String fieldIdName) {
        return from(tableName)
                .whereCondition(selectByIdWhereCondition(fieldIdName))
                .buildSelectStatement();
    }

    public String update(Object entity, String tableName, String fieldIdName, List<ColumnSnapshot> diff) {
        var entityClass = entity.getClass();
        var update = UpdateQueryBuilder.update(tableName);

        if (!isDynamicUpdate(entityClass)) {
            var declaredFields = entityClass.getDeclaredFields();
            Arrays.stream(declaredFields)
                    .map(EntityReflectionUtils::columnName)
                    .filter(fieldName -> !fieldName.equals(fieldIdName))
                    .forEach(fieldName -> update.setField(fieldName, PARAMETER));

            return update.whereCondition(selectByIdWhereCondition(fieldIdName))
                    .buildUpdateStatement();
        }


        diff.stream()
                .map(ColumnSnapshot::name)
                .filter(fieldName -> !fieldName.equals(fieldIdName))
                .forEach(fieldName -> update.setField(fieldName, PARAMETER));

        return update.whereCondition(selectByIdWhereCondition(fieldIdName))
                .buildUpdateStatement();
    }
    
    public String selectByIdWhereCondition(String fieldIdName) {
        return fieldIdName + EQ + PARAMETER;
    }
    

    public String insert(Object entity, String tableName) {
        var insert = InsertQueryBuilder.from(tableName);
        getInsertEntityFields(entity).forEach(field -> insert.setField(columnName(field)));

        return insert.buildInsertStatement();
    }
}
