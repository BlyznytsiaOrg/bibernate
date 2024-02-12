package io.github.blyznytsiaorg.bibernate.actionqueue;

/**
 * Enumerates different types of entity actions that can be performed, including INSERT, UPDATE, and DELETE.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public enum ActionType {

    /**
     * Represents the action of inserting a new entity.
     */
    INSERT,

    /**
     * Represents the action of updating an existing entity.
     */
    UPDATE,

    /**
     * Represents the action of deleting an existing entity.
     */
    DELETE
}
