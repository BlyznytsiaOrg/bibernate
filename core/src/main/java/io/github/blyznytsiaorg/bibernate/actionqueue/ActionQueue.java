package io.github.blyznytsiaorg.bibernate.actionqueue;

/**
 * Represents an action queue for executing entity-related actions. Implementations of this interface
 * handle the execution and addition of entity actions, allowing for organized and sequential processing
 * of actions on entities.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public interface ActionQueue {

    /**
     * Executes an entity action from the queue.
     */
    void executeEntityAction();

    /**
     * Adds an entity action to the queue for later execution.
     *
     * @param entityAction The entity action to be added to the queue.
     */
    void addEntityAction(EntityAction entityAction);

    /**
     * Checks if the action queue is set to not execute actions. When this method returns true,
     * it indicates that the action queue is in a state where it should not perform any execution
     * of entity actions.
     *
     * @return true if the action queue is set to not execute actions, false otherwise.
     */
    boolean isNotExecuted();
}
