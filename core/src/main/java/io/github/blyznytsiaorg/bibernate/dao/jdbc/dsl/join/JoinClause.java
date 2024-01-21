package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.join;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class JoinClause {
    private final String joinedTable;
    private final String onCondition;
    private final JoinType joinType;

    public JoinClause(String joinedTable, String onCondition, JoinType joinType) {
        this.joinedTable = joinedTable;
        this.onCondition = onCondition;
        this.joinType = joinType;
    }

    @Override
    public String toString() {
        return String.format("%s JOIN %s ON %s", joinType.toString(), joinedTable, onCondition);
    }
}
