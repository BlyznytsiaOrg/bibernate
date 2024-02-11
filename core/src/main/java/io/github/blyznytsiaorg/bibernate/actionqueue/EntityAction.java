package io.github.blyznytsiaorg.bibernate.actionqueue;

import java.util.Collection;

/**
 * Represents an action to be performed on entities, defining methods for execution, obtaining the entity class,
 * retrieving entities involved in the action, and determining the action type.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public interface EntityAction {

    /**
     * Executes the entity action.
     */
    void execute();

    /**
     * Gets the class of the entity associated with the action.
     *
     * @return The Class object representing the entity class.
     */
    Class<?> getEntityClass();

    /**
     * Gets the collection of entities involved in the action.
     *
     * @return A collection of entities associated with the action.
     */
    Collection<?> getEntities();

    /**
     * Gets the type of action to be performed on the entities, such as INSERT, UPDATE, or DELETE.
     *
     * @return The ActionType representing the type of entity action.
     */
    ActionType getActionType();
}
