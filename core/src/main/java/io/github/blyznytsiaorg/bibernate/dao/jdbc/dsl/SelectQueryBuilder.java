package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.join.JoinClause;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.join.JoinType;
import io.github.blyznytsiaorg.bibernate.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    public SelectQueryBuilder selectFieldsFromTable(String tableName) {
        selectFieldFromTable(tableName, null);
        return this;
    }
    
    public SelectQueryBuilder selectFieldFromTable(String tableName, String fieldName) {
        Objects.requireNonNull(tableName);
        
        var notNullFieldName = Optional.ofNullable(fieldName).orElse(ALL_FIELDS);
        var tableFieldName = tableName.concat(DOT).concat(notNullFieldName);
        selectedFields.add(tableFieldName);
        
        return this;
    }

    public SelectQueryBuilder join(String joinedTable, String onCondition, JoinType joinType) {
        var joinClause = new JoinClause(joinedTable, onCondition, joinType);
        joinClauses.add(joinClause);
        return this;
    }

    public SelectQueryBuilder whereCondition(String condition) {
        if (Objects.nonNull(condition)) {
            whereConditions.add(condition);
        }
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
