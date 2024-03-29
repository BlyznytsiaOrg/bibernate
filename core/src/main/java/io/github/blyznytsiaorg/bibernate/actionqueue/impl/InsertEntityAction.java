package io.github.blyznytsiaorg.bibernate.actionqueue.impl;

import io.github.blyznytsiaorg.bibernate.actionqueue.ActionType;
import io.github.blyznytsiaorg.bibernate.actionqueue.EntityAction;
import io.github.blyznytsiaorg.bibernate.session.BibernateSession;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

/**
 * Represents an entity action for inserting entities in the Bibernate framework. This action is associated with
 * a specific entity class and a collection of entities to be inserted.
 * <p>
 * Upon execution, the action inserts each entity in the collection into the data store.
 *
 * @param <T> The generic type representing the entity class.
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Builder
@RequiredArgsConstructor
public class InsertEntityAction<T> implements EntityAction {

    /**
     * The Bibernate session associated with the InsertEntityAction, providing access to the underlying data access
     * operations and the execution context for the insert action.
     */
    private final BibernateSession bibernateSession;
    /**
     * The Class object representing the type of entities to be inserted.
     */
    @Getter
    private final Class<T> entityClass;
    /**
     * A collection of entities to be inserted.
     */
    private final Collection<T> entities;

    /**
     * Executes the entity action by inserting each entity in the collection into the data store.
     */
    @Override
    public void execute() {
        entities.forEach(entity -> bibernateSession.save(entityClass, entity));
    }

    /**
     * Gets the collection of entities to be inserted.
     *
     * @return A collection of entities associated with the insert action.
     */
    @Override
    public Collection<T> getEntities() {
        return entities;
    }

    /**
     * Gets the type of action, which is {@code INSERT}.
     *
     * @return The ActionType representing the type of entity action.
     */
    @Override
    public ActionType getActionType() {
        return ActionType.INSERT;
    }
}
