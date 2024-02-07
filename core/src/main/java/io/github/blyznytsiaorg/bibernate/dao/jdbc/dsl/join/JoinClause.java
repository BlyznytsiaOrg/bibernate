package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.join;

/**
 * Represents a SQL JOIN clause with information about the joined table, the ON condition,
 * and the type of JOIN (INNER, LEFT, RIGHT, FULL).
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class JoinClause {

    /**
     * The name of the table to be joined.
     */
    private final String joinedTable;
    /**
     * The ON condition specifying how the tables should be joined.
     */
    private final String onCondition;
    /**
     * The type of JOIN (INNER, LEFT, RIGHT, FULL).
     */
    private final JoinType joinType;

    /**
     * Constructs a new JoinClause with the specified parameters.
     *
     * @param joinedTable The name of the table to be joined.
     * @param onCondition The ON condition specifying how the tables should be joined.
     * @param joinType The type of JOIN (INNER, LEFT, RIGHT, FULL).
     */
    public JoinClause(String joinedTable, String onCondition, JoinType joinType) {
        this.joinedTable = joinedTable;
        this.onCondition = onCondition;
        this.joinType = joinType;
    }

    /**
     * Returns a string representation of the JOIN clause.
     *
     * @return A string containing the JOIN clause in the format "{@code JOIN_TYPE JOIN joinedTable ON onCondition}".
     */
    @Override
    public String toString() {
        return String.format("%s JOIN %s ON %s", joinType.toString(), joinedTable, onCondition);
    }
}
