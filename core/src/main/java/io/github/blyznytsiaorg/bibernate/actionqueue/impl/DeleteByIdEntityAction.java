package io.github.blyznytsiaorg.bibernate.actionqueue.impl;

import io.github.blyznytsiaorg.bibernate.actionqueue.ActionType;
import io.github.blyznytsiaorg.bibernate.actionqueue.EntityAction;
import io.github.blyznytsiaorg.bibernate.session.BibernateSession;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

import static io.github.blyznytsiaorg.bibernate.actionqueue.ActionType.DELETE;

/**
 * Represents an entity action for deleting entities by their primary key in the Bibernate framework. This action is
 * associated with a specific entity class, a primary key value, and a collection of entities to be deleted.
 * <p>
 * Upon execution, the action deletes entities with the specified primary key from the data store.
 *
 * @param <T> The generic type representing the entity class.
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Builder
@RequiredArgsConstructor
public class DeleteByIdEntityAction<T> implements EntityAction {

    /**
     * The Bibernate session associated with the DeleteByIdEntityAction, providing access to the underlying data access
     * operations and the execution context for the entity deletion by primary key action.
     */
    private final BibernateSession bibernateSession;
    /**
     * The Class object representing the type of entities to be deleted.
     */
    @Getter
    private final Class<T> entityClass;
    /**
     * The primary key value used to identify and delete entities.
     */
    private final Object primaryKey;
    /**
     * A collection of entities associated with the delete action.
     */
    private final Collection<T> entities;
    /**
     * Runnable to remove cache and snapshot.
     */
    private final Runnable removeCacheAndSnapshot;

    /**
     * Executes the entity action by deleting entities with the specified primary key from the data store.
     */
    @Override
    public void execute() {
        entities.forEach(e -> bibernateSession.deleteById(entityClass, primaryKey));
        removeCacheAndSnapshot.run();
    }

    /**
     * Gets the collection of entities to be deleted.
     *
     * @return A collection of entities associated with the delete action.
     */
    @Override
    public Collection<T> getEntities() {
        return entities;
    }

    /**
     * Gets the type of action, which is {@code DELETE}.
     *
     * @return The ActionType representing the type of entity action.
     */
    @Override
    public ActionType getActionType() {
        return DELETE;
    }
}
