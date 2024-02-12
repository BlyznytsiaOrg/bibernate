package io.github.blyznytsiaorg.bibernate.actionqueue.impl;

import io.github.blyznytsiaorg.bibernate.actionqueue.ActionType;
import io.github.blyznytsiaorg.bibernate.actionqueue.EntityAction;
import io.github.blyznytsiaorg.bibernate.session.BibernateSession;
import io.github.blyznytsiaorg.bibernate.utils.CollectionUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

/**
 * Represents an entity action for inserting a collection of entities in the Bibernate framework. This action is
 * associated with a specific entity class and a collection of entities to be inserted.
 * <p>
 * Upon execution, the action inserts all entities in the collection into the data store using a batch insert operation.
 *
 * @param <T> The generic type representing the entity class.
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Builder
@RequiredArgsConstructor
public class InsertAllEntityAction<T> implements EntityAction {

    /**
     * The Bibernate session associated with the InsertAllEntityAction, providing access to the underlying data access
     * operations and the execution context for the batch insert action.
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
     * Executes the entity action by inserting all entities in the collection into the data store using a batch insert operation.
     * If the collection is empty, no operation is performed.
     */
    @Override
    public void execute() {
        if (CollectionUtils.isNotEmpty(entities)) {
            bibernateSession.saveAll(entityClass, entities);
        }
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
