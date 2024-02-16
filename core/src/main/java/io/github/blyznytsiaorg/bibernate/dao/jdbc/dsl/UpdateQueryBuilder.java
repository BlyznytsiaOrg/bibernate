package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import io.github.blyznytsiaorg.bibernate.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a SQL UPDATE query builder for constructing UPDATE statements with specified fields and conditions.
 * Extends the base class QueryBuilder.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class UpdateQueryBuilder extends QueryBuilder {

    private static final String ONE = "1";
    private static final String PLUS = " + ";
    /**
     * The list of fields to be updated with their corresponding values.
     */
    private final List<UpdateField> updateFields;

    /**
     * Constructs a new UpdateQueryBuilder with the specified table name.
     *
     * @param tableName The name of the table to be updated.
     */
    public UpdateQueryBuilder(String tableName) {
        super(tableName, new ArrayList<>());
        this.updateFields = new ArrayList<>();
    }

    /**
     * Creates a new UpdateQueryBuilder with the specified table name.
     *
     * @param tableName The name of the table to be updated.
     * @return A new instance of UpdateQueryBuilder.
     */
    public static UpdateQueryBuilder update(String tableName) {
        return new UpdateQueryBuilder(tableName);
    }

    /**
     * Adds a field to be updated with an incremented value.
     * Useful for versioning fields, e.g., setFieldIncrementVersion("version").
     *
     * @param fieldName The name of the field to be updated.
     * @return The current UpdateQueryBuilder instance for method chaining.
     */
    public UpdateQueryBuilder setFieldIncrementVersion(String fieldName) {
        var updateField = new UpdateField(fieldName, fieldName + PLUS + ONE);
        updateFields.add(updateField);
        return this;
    }

    /**
     * Adds a field to be updated with the specified value.
     *
     * @param fieldName The name of the field to be updated.
     * @param value     The new value for the field.
     * @return The current UpdateQueryBuilder instance for method chaining.
     */
    public UpdateQueryBuilder setField(String fieldName, Object value) {
        var updateField = new UpdateField(fieldName, value);
        updateFields.add(updateField);
        return this;
    }

    /**
     * Adds a WHERE condition to the UPDATE statement.
     *
     * @param condition The WHERE condition to be added.
     * @return The current UpdateQueryBuilder instance for method chaining.
     */
    public UpdateQueryBuilder whereCondition(String condition) {
        whereConditions.add(condition);
        return this;
    }

    /**
     * Adds an AND condition to the existing WHERE conditions.
     *
     * @param condition The AND condition to be added.
     * @return The current UpdateQueryBuilder instance for method chaining.
     */
    public UpdateQueryBuilder andCondition(String condition) {
        whereConditions.add(AND + condition);
        return this;
    }

    /**
     * Adds an OR condition to the existing WHERE conditions.
     *
     * @param condition The OR condition to be added.
     * @return The current UpdateQueryBuilder instance for method chaining.
     */
    public UpdateQueryBuilder orCondition(String condition) {
        whereConditions.add(OR + condition);
        return this;
    }

    /**
     * Adds a BETWEEN condition to the existing WHERE conditions.
     *
     * @param field      The field to which the BETWEEN condition applies.
     * @param lowerBound The lower bound of the range.
     * @param upperBound The upper bound of the range.
     * @return The current UpdateQueryBuilder instance for method chaining.
     */
    public UpdateQueryBuilder betweenCondition(String field, Object lowerBound, Object upperBound) {
        whereConditions.add(String.format(BETWEEN_S_AND_S, field, lowerBound, upperBound));
        return this;
    }

    /**
     * Builds the UPDATE SQL statement based on the configured fields and conditions.
     *
     * @return The generated UPDATE SQL statement as a string.
     * @throws IllegalStateException If no fields are specified for update.
     */
    public String buildUpdateStatement() {
        if (CollectionUtils.isEmpty(updateFields)) {
            throw new IllegalStateException("No fields specified for update.");
        }

        var queryBuilder = new StringBuilder(UPDATE);
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

    /**
     * Represents an individual field to be updated with its corresponding value.
     */
    public static class UpdateField {

        /**
         * The name of the field to be updated.
         */
        private final String fieldName;
        /**
         * The new value for the field.
         */
        private final Object value;

        /**
         * Constructs a new UpdateField with the specified field name and value.
         *
         * @param fieldName The name of the field to be updated.
         * @param value     The new value for the field.
         */
        public UpdateField(String fieldName, Object value) {
            this.fieldName = fieldName;
            this.value = value;
        }
    }
}
