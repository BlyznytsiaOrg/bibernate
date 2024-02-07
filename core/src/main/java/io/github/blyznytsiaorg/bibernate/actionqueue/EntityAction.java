package io.github.blyznytsiaorg.bibernate.actionqueue;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public abstract class EntityAction {
    public abstract void execute();

    public abstract Class<?> getEntityClass();

    public abstract ActionType getActionType();
}
