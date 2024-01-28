package io.github.blyznytsiaorg.bibernate.dao.utils;

import io.github.blyznytsiaorg.bibernate.entity.EntityColumn;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.SelectQueryBuilder.from;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getEntityFields;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.table;


/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@ToString
@Getter
public class HqlQueryInfo {
    private static final Pattern SELECT_PATTERN = Pattern.compile("SELECT\\s+(\\w+)\\s+FROM\\s+(\\w+)\\s+");
    private static final Pattern JOIN_FETCH_PATTERN = Pattern.compile("JOIN FETCH\\s+(\\w+\\.\\w+)\\s+");
    private static final Pattern JOIN_PATTERN = Pattern.compile("JOIN\\s+(\\w+\\.\\w+)\\s+");
    private static final  Pattern WHERE_PATTERN = Pattern.compile("WHERE\\s+(.+)");
    public static final String DOT = ".";

    private String entityName;
    private String entityAlias;
    private String conditions;
    private final List<String> joins = new ArrayList<>();
    private final List<String> fetchJoins = new ArrayList<>();
    private final Class<?> entityClass;

    public HqlQueryInfo(String hqlQuery, Class<?> entityClass) {
        Matcher selectMatcher = SELECT_PATTERN.matcher(hqlQuery);
        Matcher joinFetchMatcher = JOIN_FETCH_PATTERN.matcher(hqlQuery);
        Matcher joinMatcher = JOIN_PATTERN.matcher(hqlQuery);
        Matcher whereMatcher = WHERE_PATTERN.matcher(hqlQuery);

        // Extract entity name and alias from SELECT clause
        if (selectMatcher.find()) {
           setEntityAlias(selectMatcher.group(1));
           setEntityName(selectMatcher.group(2));
        }

        // Extract JOIN FETCH clauses
        while (joinFetchMatcher.find()) {
            addFetchJoin(joinFetchMatcher.group(1));
        }

        // Extract JOIN clauses
        while (joinMatcher.find()) {
            addJoin(joinMatcher.group(1));
        }

        // Extract conditions from WHERE clause
        if (whereMatcher.find()) {
            setConditions(whereMatcher.group(1));
        }

        this.entityClass = entityClass;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public void setEntityAlias(String entityAlias) {
        this.entityAlias = entityAlias;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public void addJoin(String join) {
        joins.add(join);
    }

    public void addFetchJoin(String fetchJoin) {
        fetchJoins.add(fetchJoin);
    }

    public String toNativeSql() {
        var tableName = table(entityClass);

        for (EntityColumn entityColumn : getEntityFields(entityClass)) {
            conditions = conditions.replace(entityAlias + DOT + entityColumn.fieldName(), entityColumn.fieldColumnName());
        }

        return from(tableName)
                .whereCondition(conditions)
                .buildSelectStatement();
    }
}
