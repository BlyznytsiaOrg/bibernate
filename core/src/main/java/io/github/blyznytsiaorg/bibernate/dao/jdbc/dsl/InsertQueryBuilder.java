package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import io.github.blyznytsiaorg.bibernate.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a SQL INSERT query builder for constructing INSERT statements with specified fields and values.
 * Extends the base class QueryBuilder.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class InsertQueryBuilder extends QueryBuilder {

    /**
     * Represents an individual field to be inserted with its corresponding value.
     */
    private final List<InsertField> insertFields;

    /**
     * The list of fields to be inserted along with their values.
     */
    public InsertQueryBuilder(String tableName) {
        super(tableName, new ArrayList<>());
        this.insertFields = new ArrayList<>();
    }

    /**
     * Constructs a new InsertQueryBuilder with the specified table name.
     *
     * @param tableName The name of the table into which records will be inserted.
     */
    public static InsertQueryBuilder from(String tableName) {
        return new InsertQueryBuilder(tableName);
    }

    /**
     * Adds a field to the list of fields to be inserted.
     *
     * @param fieldName The name of the field to be inserted.
     * @return The current InsertQueryBuilder instance for method chaining.
     */
    public InsertQueryBuilder setField(String fieldName) {
        var updateField = new InsertField(fieldName);
        insertFields.add(updateField);
        return this;
    }

    /**
     * Builds the INSERT SQL statement based on the configured fields and values.
     *
     * @return The generated INSERT SQL statement as a string.
     * @throws IllegalStateException If no fields are specified for insert.
     */
    public String buildInsertStatement() {
        if (CollectionUtils.isEmpty(insertFields)) {
            throw new IllegalStateException("No fields specified for insert.");
        }

        var fieldNames = insertFields.stream()
                .map(insertField -> insertField.fieldName)
                .collect(Collectors.joining(COMA));
        var values = insertFields.stream()
                .map(insertField -> PARAMETER)
                .collect(Collectors.joining(COMA));

        return INSERT_INTO.formatted(tableName, fieldNames, values);
    }

    record InsertField(String fieldName) {
    }
}
