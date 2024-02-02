package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a SQL DELETE query builder for constructing DELETE statements with optional WHERE conditions.
 * Extends the base class QueryBuilder.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class DeleteQueryBuilder extends QueryBuilder {

    /**
     * Constructs a new DeleteQueryBuilder with the specified table name.
     *
     * @param tableName The name of the table from which to delete records.
     */
    public DeleteQueryBuilder(String tableName) {
        super(tableName, new ArrayList<>());
    }

    /**
     * Creates a new DeleteQueryBuilder with the specified table name.
     *
     * @param tableName The name of the table from which to delete records.
     * @return A new instance of DeleteQueryBuilder.
     */
    public static DeleteQueryBuilder from(String tableName) {
        return new DeleteQueryBuilder(tableName);
    }

    /**
     * Adds a WHERE condition to the DELETE query.
     *
     * @param condition The WHERE condition to be added.
     * @return The current DeleteQueryBuilder instance for method chaining.
     */
    public DeleteQueryBuilder whereCondition(String condition) {
        if (Objects.nonNull(condition)) {
            whereConditions.add(condition);
        }
        return this;
    }

    /**
     * Adds an AND condition to the existing WHERE conditions.
     *
     * @param condition The AND condition to be added.
     * @return The current DeleteQueryBuilder instance for method chaining.
     */
    public DeleteQueryBuilder andCondition(String condition) {
        whereConditions.add(AND + condition);
        return this;
    }

    /**
     * Adds an OR condition to the existing WHERE conditions.
     *
     * @param condition The OR condition to be added.
     * @return The current DeleteQueryBuilder instance for method chaining.
     */
    public DeleteQueryBuilder orCondition(String condition) {
        whereConditions.add(OR + condition);
        return this;
    }

    /**
     * Adds a BETWEEN condition to the existing WHERE conditions.
     *
     * @param field The field to which the BETWEEN condition applies.
     * @param lowerBound The lower bound of the range.
     * @param upperBound The upper bound of the range.
     * @return The current DeleteQueryBuilder instance for method chaining.
     */
    public DeleteQueryBuilder betweenCondition(String field, Object lowerBound, Object upperBound) {
        whereConditions.add(String.format(BETWEEN_S_AND_S, field, lowerBound, upperBound));
        return this;
    }

    /**
     * Builds the DELETE SQL statement based on the configured conditions.
     *
     * @return The generated DELETE SQL statement as a string.
     */
    public String buildDeleteStatement() {
        var queryBuilder = new StringBuilder(DELETE)
                .append(FROM)
                .append(tableName);
        handleWhereCondition(queryBuilder);
        queryBuilder.append(SEMICOLON);

        return queryBuilder.toString();
    }
}
