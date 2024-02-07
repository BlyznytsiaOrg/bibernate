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
 * Represents information extracted from an HQL (Bibernate Query Language) query.
 * Parses the SELECT, JOIN FETCH, JOIN, and WHERE clauses to provide details about the HQL query.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@ToString
@Getter
public class HqlQueryInfo {
    /**
     * Regular expression pattern for extracting SELECT clause from an HQL query.
     */
    private static final Pattern SELECT_PATTERN = Pattern.compile("SELECT\\s+(\\w+)\\s+FROM\\s+(\\w+)\\s+");

    /**
     * Regular expression pattern for extracting JOIN FETCH clauses from an HQL query.
     */
    private static final Pattern JOIN_FETCH_PATTERN = Pattern.compile("JOIN FETCH\\s+(\\w+\\.\\w+)\\s+");

    /**
     * Regular expression pattern for extracting JOIN clauses from an HQL query.
     */
    private static final Pattern JOIN_PATTERN = Pattern.compile("JOIN\\s+(\\w+\\.\\w+)\\s+");

    /**
     * Regular expression pattern for extracting WHERE clause from an HQL query.
     */
    private static final Pattern WHERE_PATTERN = Pattern.compile("WHERE\\s+(.+)");

    /**
     * Dot separator constant used in constructing entity aliases.
     */
    public static final String DOT = ".";

    private String entityName;
    private String entityAlias;
    private String conditions;
    private final List<String> joins = new ArrayList<>();
    private final List<String> fetchJoins = new ArrayList<>();
    private final Class<?> entityClass;

    /**
     * Constructs an instance of HqlQueryInfo by parsing the provided HQL query and associating it with a specified entity class.
     *
     * @param hqlQuery    The HQL query to be parsed.
     * @param entityClass The entity class associated with the HQL query.
     */
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

    /**
     * Sets the entity name obtained from the SELECT clause.
     *
     * @param entityName The entity name.
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * Sets the entity alias obtained from the SELECT clause.
     *
     * @param entityAlias The entity alias.
     */
    public void setEntityAlias(String entityAlias) {
        this.entityAlias = entityAlias;
    }

    /**
     * Sets the conditions obtained from the WHERE clause.
     *
     * @param conditions The conditions.
     */
    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    /**
     * Adds a JOIN clause to the list of joins.
     *
     * @param join The JOIN clause.
     */
    public void addJoin(String join) {
        joins.add(join);
    }

    /**
     * Adds a JOIN FETCH clause to the list of fetch joins.
     *
     * @param fetchJoin The JOIN FETCH clause.
     */
    public void addFetchJoin(String fetchJoin) {
        fetchJoins.add(fetchJoin);
    }

    /**
     * Converts the HQL query information into native SQL format based on the associated entity class.
     *
     * @return The native SQL representation of the HQL query information.
     */
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
