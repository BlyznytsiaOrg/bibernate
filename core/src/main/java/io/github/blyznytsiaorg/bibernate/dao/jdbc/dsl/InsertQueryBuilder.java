package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import io.github.blyznytsiaorg.bibernate.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class InsertQueryBuilder extends QueryBuilder {

    private final List<InsertField> insertFields;

    public InsertQueryBuilder(String tableName) {
        super(tableName, new ArrayList<>());
        this.insertFields = new ArrayList<>();
    }

    public static InsertQueryBuilder from(String tableName) {
        return new InsertQueryBuilder(tableName);
    }

    public InsertQueryBuilder setField(String fieldName) {
        var updateField = new InsertField(fieldName);
        insertFields.add(updateField);
        return this;
    }

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
