package io.github.blyznytsiaorg.bibernate.actionqueue.impl;

import io.github.blyznytsiaorg.bibernate.actionqueue.ActionType;
import io.github.blyznytsiaorg.bibernate.actionqueue.EntityAction;
import io.github.blyznytsiaorg.bibernate.session.BibernateSession;
import io.github.blyznytsiaorg.bibernate.utils.CollectionUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

import static io.github.blyznytsiaorg.bibernate.actionqueue.ActionType.DELETE;

/**
 * Represents an entity action for bulk deletion of entities of a specific class.
 * This action is associated with a Bibernate session and executes the deletion operation
 * for the provided collection of entities within the specified entity class.
 *
 * @param <T> The generic type representing the entity class.
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Builder
@RequiredArgsConstructor
public class DeleteAllEntityAction<T> implements EntityAction {

    /**
     * The Bibernate session associated with the DeleteAllEntityAction, providing access to the
     * underlying data access operations and serving as the execution context for the bulk entity deletion action.
     */
    private final BibernateSession bibernateSession;
    /**
     * The class of entities targeted for deletion by this action.
     */
    @Getter
    private final Class<T> entityClass;
    /**
     * The collection of entities to be deleted by this action.
     */
    private final Collection<T> entities;
    /**
     * Runnable to remove cache and snapshot.
     */
    private final Runnable removeCacheAndSnapshot;

    /**
     * Executes the bulk entity deletion action by invoking the corresponding method in the associated Bibernate session.
     * This method checks for a non-empty collection of entities before performing the deletion operation.
     */
    @Override
    public void execute() {
        if (CollectionUtils.isNotEmpty(entities)) {
            bibernateSession.deleteAll(entityClass, entities);
        }
        removeCacheAndSnapshot.run();
    }

    /**
     * Retrieves the collection of entities targeted for deletion by this action.
     *
     * @return The collection of entities.
     */
    @Override
    public Collection<T> getEntities() {
        return entities;
    }

    /**
     * Retrieves the ActionType associated with this entity action, indicating the type of operation (DELETE).
     *
     * @return The ActionType associated with this entity action.
     */
    @Override
    public ActionType getActionType() {
        return DELETE;
    }
}
