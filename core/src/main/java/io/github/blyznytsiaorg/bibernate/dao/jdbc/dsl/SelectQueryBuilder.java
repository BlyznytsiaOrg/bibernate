package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.join.JoinClause;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.join.JoinType;
import io.github.blyznytsiaorg.bibernate.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a SQL SELECT query builder for constructing SELECT statements with optional clauses
 * such as JOIN, WHERE, GROUP BY, HAVING, and UNION.
 * Extends the base class QueryBuilder.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class SelectQueryBuilder extends QueryBuilder {
    /**
     * The field to be used for GROUP BY clauses.
     */
    private String groupByField;
    /**
     * The condition for HAVING clauses.
     */
    private String havingCondition;
    /**
     * The list of fields to be selected in the SELECT statement.
     */
    private final List<String> selectedFields;
    /**
     * The list of JOIN clauses to be included in the SELECT statement.
     */
    private final List<JoinClause> joinClauses;
    /**
     * The list of SELECT queries to be combined using UNION.
     */
    private final List<SelectQueryBuilder> unionQueries;

    /**
     * Constructs a new SelectQueryBuilder with the specified table name.
     *
     * @param tableName The name of the table from which to select records.
     */
    public SelectQueryBuilder(String tableName) {
        super(tableName, new ArrayList<>());
        this.selectedFields = new ArrayList<>();
        this.joinClauses = new ArrayList<>();
        this.unionQueries = new ArrayList<>();
    }

    /**
     * Creates a new SelectQueryBuilder with the specified table name.
     *
     * @param tableName The name of the table from which to select records.
     * @return A new instance of SelectQueryBuilder.
     */
    public static SelectQueryBuilder from(String tableName) {
        return new SelectQueryBuilder(tableName);
    }

    /**
     * Adds a field to the list of fields to be selected in the SELECT statement.
     *
     * @param fieldName The name of the field to be selected.
     * @return The current SelectQueryBuilder instance for method chaining.
     */
    public SelectQueryBuilder selectField(String fieldName) {
        selectedFields.add(fieldName);
        return this;
    }

    public SelectQueryBuilder selectFields(List<String> fieldNames) {
        selectedFields.addAll(fieldNames);
        return this;
    }

    /**
     * Adds all fields from a specified table to the list of fields to be selected.
     *
     * @param tableName The name of the table from which to select all fields.
     * @return The current SelectQueryBuilder instance for method chaining.
     */
    public SelectQueryBuilder selectFieldsFromTable(String tableName) {
        selectFieldFromTable(tableName, null);
        return this;
    }

    /**
     * Adds a specific field from a specified table to the list of fields to be selected.
     *
     * @param tableName The name of the table from which to select the field.
     * @param fieldName The name of the field to be selected.
     * @return The current SelectQueryBuilder instance for method chaining.
     */
    public SelectQueryBuilder selectFieldFromTable(String tableName, String fieldName) {
        Objects.requireNonNull(tableName);

        var notNullFieldName = Optional.ofNullable(fieldName).orElse(ALL_FIELDS);
        var tableFieldName = tableName.concat(DOT).concat(notNullFieldName);
        selectedFields.add(tableFieldName);

        return this;
    }

    /**
     * Adds a JOIN clause to the SELECT statement.
     *
     * @param joinedTable The name of the table to be joined.
     * @param onCondition The ON condition specifying how the tables should be joined.
     * @param joinType     The type of JOIN (INNER, LEFT, RIGHT, FULL).
     * @return The current SelectQueryBuilder instance for method chaining.
     */
    public SelectQueryBuilder join(String joinedTable, String onCondition, JoinType joinType) {
        var joinClause = new JoinClause(joinedTable, onCondition, joinType);
        joinClauses.add(joinClause);
        return this;
    }

    /**
     * Adds a WHERE condition to the SELECT statement.
     *
     * @param condition The WHERE condition to be added.
     * @return The current SelectQueryBuilder instance for method chaining.
     */
    public SelectQueryBuilder whereCondition(String condition) {
        if (Objects.nonNull(condition)) {
            whereConditions.add(condition);
        }
        return this;
    }

    /**
     * Adds an AND condition to the existing WHERE conditions.
     *
     * @param condition The AND condition to be added.
     * @return The current SelectQueryBuilder instance for method chaining.
     */
    public SelectQueryBuilder andCondition(String condition) {
        whereConditions.add(AND + condition);
        return this;
    }

    /**
     * Adds an OR condition to the existing WHERE conditions.
     *
     * @param condition The OR condition to be added.
     * @return The current SelectQueryBuilder instance for method chaining.
     */
    public SelectQueryBuilder orCondition(String condition) {
        whereConditions.add(OR + condition);
        return this;
    }

    /**
     * Adds a BETWEEN condition to the existing WHERE conditions.
     *
     * @param field      The field to which the BETWEEN condition applies.
     * @param lowerBound The lower bound of the range.
     * @param upperBound The upper bound of the range.
     * @return The current SelectQueryBuilder instance for method chaining.
     */
    public SelectQueryBuilder betweenCondition(String field, Object lowerBound, Object upperBound) {
        whereConditions.add(String.format(BETWEEN_S_AND_S, field, lowerBound, upperBound));
        return this;
    }

    /**
     * Sets the field for GROUP BY clauses in the SELECT statement.
     *
     * @param groupByField The field to be used for GROUP BY clauses.
     * @return The current SelectQueryBuilder instance for method chaining.
     */
    public SelectQueryBuilder groupBy(String groupByField) {
        this.groupByField = groupByField;
        return this;
    }

    /**
     * Sets the condition for HAVING clauses in the SELECT statement.
     *
     * @param havingCondition The condition for HAVING clauses.
     * @return The current SelectQueryBuilder instance for method chaining.
     */
    public SelectQueryBuilder havingCondition(String havingCondition) {
        this.havingCondition = havingCondition;
        return this;
    }

    /**
     * Combines the current SELECT query with another query using the UNION operator.
     *
     * @param otherQuery The SelectQueryBuilder representing the other query.
     * @return The current SelectQueryBuilder instance for method chaining.
     */
    public SelectQueryBuilder union(SelectQueryBuilder otherQuery) {
        unionQueries.add(otherQuery);
        return this;
    }

    /**
     * Builds the SELECT SQL statement based on the configured conditions and clauses.
     *
     * @return The generated SELECT SQL statement as a string.
     */
    public String buildSelectStatement() {
        var queryBuilder = new StringBuilder(SELECT);

        queryBuilder.append(CollectionUtils.isEmpty(selectedFields) ? ALL_FIELDS : String.join(COMA, selectedFields));

        queryBuilder.append(FROM).append(tableName);

        joinClauses.forEach(joinClause -> queryBuilder.append(SPACE).append(joinClause.toString()));

        handleWhereCondition(queryBuilder);

        if (Objects.nonNull(groupByField) && !groupByField.isEmpty()) {
            queryBuilder.append(GROUP_BY).append(groupByField);
        }

        if (Objects.nonNull(havingCondition) && !havingCondition.isEmpty()) {
            queryBuilder.append(HAVING).append(havingCondition);
        }

        if (CollectionUtils.isNotEmpty(unionQueries)) {
            queryBuilder.append(UNION);
            unionQueries.forEach(unionQuery -> 
                    queryBuilder.append(unionQuery.buildSelectStatement()).append(SPACE));
        }

        queryBuilder.append(SEMICOLON);

        return queryBuilder.toString();
    }
}
