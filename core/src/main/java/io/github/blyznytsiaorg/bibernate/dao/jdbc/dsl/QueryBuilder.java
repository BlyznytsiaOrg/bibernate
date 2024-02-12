package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import io.github.blyznytsiaorg.bibernate.utils.CollectionUtils;

import java.util.List;

/**
 * An abstract class representing a generic SQL query builder with common constants and methods
 * for constructing SQL statements such as SELECT, UPDATE, DELETE, etc.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
abstract class QueryBuilder {
    /**
     * The SQL template for INSERT INTO statements with placeholders for table name, field names, and values.
     */
    public static final String INSERT_INTO = "INSERT INTO %s ( %s ) VALUES ( %s );";
    /**
     * The SQL keyword for SELECT statements.
     */
    public static final String SELECT = "SELECT ";
    /**
     * The SQL keyword for UPDATE statements.
     */
    public static final String UPDATE = "UPDATE ";
    /**
     * The SQL keyword for DELETE statements.
     */
    public static final String DELETE = "DELETE";

    /**
     * The SQL keyword for SET clauses in UPDATE statements.
     */
    public static final String SET = " SET ";

    /**
     * The wildcard character representing all fields in SELECT statements.
     */
    public static final String ALL_FIELDS = "*";

    /**
     * The SQL keyword for FROM clauses in SELECT statements.
     */
    public static final String FROM = " FROM ";

    /**
     * The SQL keyword for WHERE clauses.
     */
    public static final String WHERE = " WHERE ";

    /**
     * The SQL keyword for GROUP BY clauses.
     */
    public static final String GROUP_BY = " GROUP BY ";

    /**
     * The SQL keyword for HAVING clauses.
     */
    public static final String HAVING = " HAVING ";

    /**
     * The SQL keyword for UNION clauses.
     */
    public static final String UNION = " UNION ";

    /**
     * The comma used to separate elements in SQL statements.
     */
    public static final String COMA = ", ";

    /**
     * The dot used to separate table and column names in SQL statements.
     */
    public static final String DOT = ".";

    /**
     * The underscore is used to sepa
     */
    public static final String UNDERSCORE = "_";

    /**
     * The semicolon used to terminate SQL statements.
     */
    public static final String SEMICOLON = ";";

    /**
     * The space character used in SQL statements for better readability.
     */
    public static final String SPACE = " ";

    /**
     * The logical AND operator for combining conditions in WHERE clauses.
     */
    public static final String AND = "AND ";

    /**
     * The AS for aliases
     */
    public static final String AS = "AS";

    /**
     * The logical OR operator for combining conditions in WHERE clauses.
     */
    public static final String OR = "OR ";

    /**
     * The logical IN operator for combining conditions in WHERE clauses.
     */
    public static final String IN = " IN ";

    /**
     * Represents the open parenthesis "(" in SQL.
     */
    public static final String OPEN_BRACKET = "( ";

    /**
     * Represents the close parenthesis ")" in SQL.
     */
    public static final String CLOSE_BRACKET = " )";

    /**
     * The equality operator used in WHERE clauses.
     */
    public static final String EQ = " = ";

    /**
     * The parameter placeholder used for prepared statements.
     */
    public static final String PARAMETER = "?";

    /**
     * The template for BETWEEN conditions with placeholders for field, lower bound, and upper bound.
     */
    public static final String BETWEEN_S_AND_S = "%s BETWEEN %s AND %s";

    /**
     * The name of the table being queried or modified.
     */
    protected final String tableName;

    /**
     * The list of conditions used in WHERE clauses.
     */
    protected final List<String> whereConditions;

    /**
     * Constructs a new QueryBuilder with the specified table name and list of WHERE conditions.
     *
     * @param tableName       The name of the table being queried or modified.
     * @param whereConditions The list of conditions used in WHERE clauses.
     */
    QueryBuilder(String tableName, List<String> whereConditions) {
        this.tableName = tableName;
        this.whereConditions = whereConditions;
    }

    /**
     * Appends the WHERE clause to the provided query builder if there are any conditions specified.
     *
     * @param queryBuilder The StringBuilder to which the WHERE clause is appended.
     */
    protected void handleWhereCondition(StringBuilder queryBuilder) {
        if (CollectionUtils.isNotEmpty(whereConditions)) {
            queryBuilder.append(WHERE).append(String.join(SPACE, whereConditions));
        }
    }
}
