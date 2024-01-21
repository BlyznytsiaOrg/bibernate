package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.join.JoinClause;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.join.JoinType;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class SelectQueryBuilder extends QueryBuilder {
    private String groupByField;
    private String havingCondition;
    private final List<String> selectedFields;
    private final List<JoinClause> joinClauses;
    private final List<SelectQueryBuilder> unionQueries;

    public SelectQueryBuilder(String tableName) {
        super(tableName, new ArrayList<>());
        this.selectedFields = new ArrayList<>();
        this.joinClauses = new ArrayList<>();
        this.unionQueries = new ArrayList<>();
    }

    public static SelectQueryBuilder from(String tableName) {
        return new SelectQueryBuilder(tableName);
    }

    public SelectQueryBuilder selectField(String fieldName) {
        selectedFields.add(fieldName);
        return this;
    }

    public SelectQueryBuilder join(String joinedTable, String onCondition, JoinType joinType) {
        var joinClause = new JoinClause(joinedTable, onCondition, joinType);
        joinClauses.add(joinClause);
        return this;
    }

    public SelectQueryBuilder whereCondition(String condition) {
        whereConditions.add(condition);
        return this;
    }

    public SelectQueryBuilder andCondition(String condition) {
        whereConditions.add(AND + condition);
        return this;
    }

    public SelectQueryBuilder orCondition(String condition) {
        whereConditions.add(OR + condition);
        return this;
    }

    public SelectQueryBuilder betweenCondition(String field, Object lowerBound, Object upperBound) {
        whereConditions.add(String.format(BETWEEN_S_AND_S, field, lowerBound, upperBound));
        return this;
    }

    public SelectQueryBuilder groupBy(String groupByField) {
        this.groupByField = groupByField;
        return this;
    }

    public SelectQueryBuilder havingCondition(String havingCondition) {
        this.havingCondition = havingCondition;
        return this;
    }

    public SelectQueryBuilder union(SelectQueryBuilder otherQuery) {
        unionQueries.add(otherQuery);
        return this;
    }

    public String buildSelectStatement() {
        var queryBuilder = new StringBuilder(SELECT);

        if (selectedFields.isEmpty()) {
            queryBuilder.append(ALL_FIELDS);
        } else {
            for (int i = 0; i < selectedFields.size(); i++) {
                queryBuilder.append(selectedFields.get(i));
                if (i < selectedFields.size() - 1) {
                    queryBuilder.append(COMA);
                }
            }
        }

        queryBuilder.append(FROM).append(tableName);

        for (var joinClause : joinClauses) {
            queryBuilder.append(SPACE).append(joinClause.toString());
        }

        handleWhereCondition(queryBuilder);

        if (groupByField != null && !groupByField.isEmpty()) {
            queryBuilder.append(GROUP_BY).append(groupByField);
        }

        if (havingCondition != null && !havingCondition.isEmpty()) {
            queryBuilder.append(HAVING).append(havingCondition);
        }

        if (!unionQueries.isEmpty()) {
            queryBuilder.append(UNION);
            for (var unionQuery : unionQueries) {
                queryBuilder.append(unionQuery.buildSelectStatement()).append(SPACE);
            }
        }

        queryBuilder.append(";");

        return queryBuilder.toString();
    }
}
