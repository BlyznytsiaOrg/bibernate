package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import io.github.blyznytsiaorg.bibernate.utils.CollectionUtils;

import java.util.List;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
abstract class QueryBuilder {
    public static final String INSERT_INTO = "INSERT INTO %s ( %s ) VALUES ( %s );";
    public static final String SELECT = "SELECT ";
    public static final String UPDATE = "UPDATE ";
    public static final String DELETE = "DELETE";
    public static final String SET = " SET ";
    public static final String ALL_FIELDS = "*";
    public static final String FROM = " FROM ";
    public static final String WHERE = " WHERE ";
    public static final String GROUP_BY = " GROUP BY ";
    public static final String HAVING = " HAVING ";
    public static final String UNION = " UNION ";
    public static final String COMA = ", ";
    public static final String SEMICOLON = ";";
    public static final String SPACE = " ";
    public static final String AND = "AND ";
    public static final String OR = "OR ";
    public static final String EQ = " = ";
    public static final String PARAMETER = "?";
    public static final String BETWEEN_S_AND_S = "%s BETWEEN %s AND %s";
    protected final String tableName;
    protected final List<String> whereConditions;

    QueryBuilder(String tableName, List<String> whereConditions) {
        this.tableName = tableName;
        this.whereConditions = whereConditions;
    }

    protected void handleWhereCondition(StringBuilder queryBuilder) {
        if (CollectionUtils.isNotEmpty(whereConditions)) {
            queryBuilder.append(WHERE).append(String.join(SPACE, whereConditions));
        }
    }
}
