package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import java.util.ArrayList;
import java.util.Objects;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class DeleteQueryBuilder extends QueryBuilder {

    public DeleteQueryBuilder(String tableName) {
        super(tableName, new ArrayList<>());
    }

    public static DeleteQueryBuilder from(String tableName) {
        return new DeleteQueryBuilder(tableName);
    }

    public DeleteQueryBuilder whereCondition(String condition) {
        if (Objects.nonNull(condition)) {
            whereConditions.add(condition);
        }
        return this;
    }

    public DeleteQueryBuilder andCondition(String condition) {
        whereConditions.add(AND + condition);
        return this;
    }

    public DeleteQueryBuilder orCondition(String condition) {
        whereConditions.add(OR + condition);
        return this;
    }

    public DeleteQueryBuilder betweenCondition(String field, Object lowerBound, Object upperBound) {
        whereConditions.add(String.format(BETWEEN_S_AND_S, field, lowerBound, upperBound));
        return this;
    }

    public String buildDeleteStatement() {
        var queryBuilder = new StringBuilder(DELETE)
                .append(FROM)
                .append(tableName);
        handleWhereCondition(queryBuilder);
        queryBuilder.append(SEMICOLON);

        return queryBuilder.toString();
    }
}
