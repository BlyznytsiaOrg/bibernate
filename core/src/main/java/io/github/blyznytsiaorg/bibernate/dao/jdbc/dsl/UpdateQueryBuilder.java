package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import io.github.blyznytsiaorg.bibernate.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class UpdateQueryBuilder extends QueryBuilder {
    
    private final List<UpdateField> updateFields;

    public UpdateQueryBuilder(String tableName) {
        super(tableName, new ArrayList<>());
        this.updateFields = new ArrayList<>();
    }

    public static UpdateQueryBuilder update(String tableName) {
        return new UpdateQueryBuilder(tableName);
    }

    public UpdateQueryBuilder setField(String fieldName, Object value) {
        var updateField = new UpdateField(fieldName, value);
        updateFields.add(updateField);
        return this;
    }

    public UpdateQueryBuilder whereCondition(String condition) {
        whereConditions.add(condition);
        return this;
    }

    public UpdateQueryBuilder andCondition(String condition) {
        whereConditions.add(AND + condition);
        return this;
    }

    public UpdateQueryBuilder orCondition(String condition) {
        whereConditions.add(OR + condition);
        return this;
    }

    public UpdateQueryBuilder betweenCondition(String field, Object lowerBound, Object upperBound) {
        whereConditions.add(String.format(BETWEEN_S_AND_S, field, lowerBound, upperBound));
        return this;
    }

    public String buildUpdateStatement() {
        if (CollectionUtils.isEmpty(updateFields)) {
            throw new IllegalStateException("No fields specified for update.");
        }

        StringBuilder queryBuilder = new StringBuilder(UPDATE);
        queryBuilder.append(tableName).append(SET);

        for (int i = 0; i < updateFields.size(); i++) {
            var updateField = updateFields.get(i);
            queryBuilder.append(updateField.fieldName).append(EQ).append(updateField.value);

            if (i < updateFields.size() - 1) {
                queryBuilder.append(COMA);
            }
        }

        handleWhereCondition(queryBuilder);

        queryBuilder.append(SEMICOLON);

        return queryBuilder.toString();
    }

    public static class UpdateField {
        
        private final String fieldName;
        private final Object value;

        public UpdateField(String fieldName, Object value) {
            this.fieldName = fieldName;
            this.value = value;
        }
    }
}
