package io.github.blyznytsiaorg.bibernate.actionqueue;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public interface ActionQueue {
    void executeEntityAction();

    void addEntityAction(EntityAction entityAction);
}
