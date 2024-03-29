package io.github.blyznytsiaorg.bibernate.dao.jdbc;

import io.github.blyznytsiaorg.bibernate.dao.JoinInfo;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.DeleteQueryBuilder;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.InsertQueryBuilder;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.SelectQueryBuilder;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.UpdateQueryBuilder;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.join.JoinClause;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.join.JoinType;
import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityColumnDetails;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.ColumnMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.JoinColumnMetadata;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.SelectQueryBuilder.*;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;
import static io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils.isInverseSide;
import static io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils.owningFieldByInverse;

/**
 * Utility class for building SQL statements (SELECT, UPDATE, INSERT, DELETE) based on different scenarios.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class SqlBuilder {

    /**
     * Generates a SELECT SQL statement for querying records from a specified table with a WHERE condition.
     *
     * @param tableName      The name of the table from which to select records.
     * @param whereCondition The WHERE condition for filtering records.
     * @return The generated SELECT SQL statement as a string.
     */
    public String selectBy(String tableName, String whereCondition) {
        return from(tableName)
                .whereCondition(whereCondition)
                .buildSelectStatement();
    }

    /**
     * Builds a SELECT statement with joins and a WHERE condition based on the provided parameters.
     *
     * @param tableName      the main table name for the SELECT statement
     * @param whereCondition the WHERE condition for filtering results
     * @param joinInfos      the information about joins to be performed
     * @param joinType       the type of join to be used (e.g., INNER JOIN, LEFT JOIN)
     * @return the dynamically generated SQL SELECT statement
     * @throws BibernateGeneralException if there is an issue retrieving column or join information
     */
    public String selectByWithJoin(String tableName,
                                   String whereCondition,
                                   Set<JoinInfo> joinInfos,
                                   JoinType joinType) {
        var joinClauses = joinInfos.stream()
                .map(joinInfo -> {
                    var parentEntityMetadata = joinInfo.getParentEntityMetadata();
                    var childEntityMetadata = joinInfo.getChildEntityMetadata();

                    var parentIdColumnName = parentEntityMetadata.getEntityColumns().stream()
                            .filter(entityColumnDetails -> Objects.nonNull(entityColumnDetails.getId()))
                            .map(EntityColumnDetails::getColumn)
                            .map(ColumnMetadata::getName)
                            .findFirst()
                            .orElseThrow(() -> new BibernateGeneralException("Cannot retrieve id column name from parent class"));

                    var childJoinColumnName = childEntityMetadata.getEntityColumns().stream()
                            .filter(entityColumnDetails -> entityColumnDetails.getFieldType().equals(parentEntityMetadata.getType()))
                            .map(EntityColumnDetails::getJoinColumn)
                            .filter(Objects::nonNull)
                            .map(JoinColumnMetadata::getName)
                            .findFirst()
                            .orElseThrow(() -> new BibernateGeneralException("Cannot retrieve join column name from child class"));

                    var onCondition = getOnCondition(parentEntityMetadata.getTableName(),
                            parentIdColumnName,
                            childEntityMetadata.getTableName(),
                            childJoinColumnName);

                    return new JoinClause(joinInfo.getJoinedTable(), onCondition, joinType);
                })
                .toList();

        return from(tableName)
                .join(joinClauses)
                .whereCondition(fieldEqualsParameterCondition(whereCondition))
                .buildSelectStatement();
    }

    /**
     * Builds a simple SELECT statement for retrieving all records from a given table.
     *
     * @param tableName the name of the table to SELECT from
     * @return the dynamically generated SQL SELECT statement
     */
    public String selectAll(String tableName) {
        return from(tableName)
                .buildSelectStatement();
    }

    /**
     * Builds a SELECT statement with a WHERE condition based on a specific field and parameter count.
     *
     * @param tableName         the main table name for the SELECT statement
     * @param filedName         the field name for the WHERE condition
     * @param countOfParameters the count of parameters for the WHERE condition
     * @return the dynamically generated SQL SELECT statement
     */
    public String selectAllByFieldName(String tableName, String filedName, Integer countOfParameters) {
        return from(tableName)
                .whereCondition(fieldInParametersCondition(filedName, countOfParameters))
                .buildSelectStatement();
    }

    /**
     * Generates an UPDATE SQL statement for updating records in a specified table based on the provided entity,
     * fieldIdName, and list of ColumnSnapshots representing the differences.
     *
     * @param entity      The entity representing the data to be updated.
     * @param tableName   The name of the table to be updated.
     * @param fieldIdName The name of the ID field used in the WHERE condition.
     * @param diff        The list of ColumnSnapshots representing differences.
     * @return The generated UPDATE SQL statement as a string.
     */
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

        var updateQueryBuilder = update.whereCondition(fieldEqualsParameterCondition(fieldIdName));
        if (isVersionFound) {
            updateQueryBuilder.andCondition(fieldVersionName + EQ + PARAMETER);
        }

        return updateQueryBuilder.buildUpdateStatement();
    }

    /**
     * Generates a field equals parameter WHERE condition for use in SQL statements.
     *
     * @param fieldName The name of the field.
     * @return The generated WHERE condition as a string.
     */
    public String fieldEqualsParameterCondition(String fieldName) {
        return fieldName + EQ + PARAMETER;
    }

    /**
     * Generates a SQL condition for a field, representing an "IN" clause with a specified number of parameters.
     *
     * @param fieldName         The name of the field for which the condition is generated.
     * @param countOfParameters The count of parameters in the "IN" clause.
     * @return A string representing the SQL condition for the "IN" clause with the specified field name and parameters.
     */
    public String fieldInParametersCondition(String fieldName, Integer countOfParameters) {
        var parameters = IntStream.range(0, countOfParameters)
                .mapToObj(i -> PARAMETER)
                .collect(Collectors.joining(COMA));

        return fieldName + IN + OPEN_BRACKET + parameters + CLOSE_BRACKET;
    }

    /**
     * Generates an INSERT SQL statement for inserting records into a specified table based on the provided entity class.
     * The method utilizes reflection to extract the fields from the entity class and constructs the INSERT statement accordingly.
     *
     * @param entityClass The Class object representing the type of the entity for which records will be inserted.
     * @param tableName   The name of the table into which records will be inserted.
     * @return The generated INSERT SQL statement as a string.
     */
    public static String insert(Class<?> entityClass, String tableName) {
        var insert = InsertQueryBuilder.from(tableName);
        getInsertEntityFields(entityClass).forEach(field -> insert.setField(columnName(field)));

        return insert.buildInsertStatement();
    }

    /**
     * Generates an INSERT SQL statement for inserting records into a specified table based on the provided table name and
     * list of column names.
     *
     * @param tableName The name of the table into which records will be inserted.
     * @param columns   The names of columns which will take part in the building of the query.
     * @return The generated INSERT SQL statement as a string.
     */
    public static String insert(String tableName, List<String> columns) {
        var insert = InsertQueryBuilder.from(tableName);
        columns.forEach(column -> insert.setField(column));

        return insert.buildInsertStatement();
    }

    /**
     * Generates a DELETE SQL statement for deleting records from a specified table based on the provided ID field.
     *
     * @param tableName   The name of the table from which records will be deleted.
     * @param fieldIdName The name of the ID field used in the WHERE condition.
     * @return The generated DELETE SQL statement as a string.
     */
    public String delete(String tableName, String fieldIdName) {
        return DeleteQueryBuilder.from(tableName)
                .whereCondition(fieldEqualsParameterCondition(fieldIdName))
                .buildDeleteStatement();
    }

    /**
     * Generates a DELETE SQL statement for deleting records from a specified table based on the provided ID field and version.
     *
     * @param tableName   The name of the table from which records will be deleted.
     * @param fieldIdName The name of the ID field used in the WHERE condition.
     * @param version     The version field used in the WHERE condition.
     * @return The generated DELETE SQL statement as a string.
     */
    public String delete(String tableName, String fieldIdName, String version) {
        return DeleteQueryBuilder.from(tableName)
                .whereCondition(fieldEqualsParameterCondition(fieldIdName))
                .andCondition(fieldEqualsParameterCondition(version))
                .buildDeleteStatement();
    }

    /**
     * Generates a SELECT SQL statement with a JOIN operation between two tables.
     *
     * @param entityTableName        The name of the entity table to be selected.
     * @param entityTableIdFieldName The name of the ID field in the entity table.
     * @param joinTableField         The field representing the join relationship.
     * @return The generated SELECT SQL statement with JOIN as a string.
     */
    public String selectWithJoin(String entityTableName, String entityTableIdFieldName,
                                 Field joinTableField) {
        var joinTableName = joinTableName(joinTableField);
        var inverseJoinColumnName = inverseTableJoinColumnName(joinTableField);
        var joinColumnName = tableJoinColumnName(joinTableField);

        if (isInverseSide(joinTableField)) {
            Field owningField = owningFieldByInverse(joinTableField);
            joinTableName = joinTableName(owningField);
            inverseJoinColumnName = tableJoinColumnName(owningField);
            joinColumnName = inverseTableJoinColumnName(owningField);
        }

        var onCondition = getOnCondition(entityTableName, entityTableIdFieldName, joinTableName, inverseJoinColumnName);

        return SelectQueryBuilder.from(joinTableName)
                .selectFieldsFromTable(entityTableName)
                .join(entityTableName, onCondition, JoinType.INNER)
                .whereCondition(fieldEqualsParameterCondition(joinColumnName))
                .buildSelectStatement();
    }

    /**
     * Merges a LEFT JOIN query with an INNER JOIN query.
     *
     * @param leftJoinQuery  the LEFT JOIN query
     * @param innerJoinQuery the INNER JOIN query
     * @return the merged query
     */
    public String mergeQueries(String leftJoinQuery, String innerJoinQuery) {
        Pattern pattern = Pattern.compile("(?i)\\bFROM\\s+(.+?)\\bINNER\\b");
        Matcher matcher = pattern.matcher(innerJoinQuery);

        if (matcher.find()) {
            String secondPart = matcher.group(1).trim();
            String firstPart = innerJoinQuery.replaceAll(".*\\b(ON.+)", "$1").trim();
            String modifiedQuery = JoinType.INNER.name() + " JOIN " + secondPart + " " + firstPart;

            String modifiedLeftJoinQuery = leftJoinQuery.replaceAll("(?i)\\s*WHERE\\s+.*", "");
            return modifiedLeftJoinQuery + " " + modifiedQuery;
        } else {
            return leftJoinQuery;
        }
    }

    /**
     * Constructs the ON condition for a JOIN operation between two tables.
     *
     * @param entityTableName        The name of the entity table participating in the JOIN.
     * @param entityTableIdFieldName The name of the ID field in the entity table.
     * @param joinTableName          The name of the table being joined with the entity table.
     * @param inverseJoinColumnName  The name of the column in the join table that corresponds to the entity table's ID.
     * @return The constructed ON condition as a string.
     */
    private static String getOnCondition(String entityTableName,
                                         String entityTableIdFieldName,
                                         String joinTableName,
                                         String inverseJoinColumnName) {
        return String.format("%s%s%s%s%s%s%s",
                entityTableName, DOT, entityTableIdFieldName, EQ, joinTableName, DOT, inverseJoinColumnName);
    }

    /**
     * Populates a field in an UPDATE operation with its corresponding value or increments the version field.
     *
     * @param fieldName             The name of the field to be updated.
     * @param isVersionFound        Indicates whether a version field is found in the entity.
     * @param finalFieldVersionName The name of the version field, if found.
     * @param update                The UpdateQueryBuilder instance for building the UPDATE statement.
     */
    private void populateFieldOrIncVersion(String fieldName, boolean isVersionFound,
                                           String finalFieldVersionName, UpdateQueryBuilder update) {
        if (isVersionFound && finalFieldVersionName.equals(fieldName)) {
            update.setFieldIncrementVersion(fieldName);
        } else {
            update.setField(fieldName, PARAMETER);
        }
    }
}
