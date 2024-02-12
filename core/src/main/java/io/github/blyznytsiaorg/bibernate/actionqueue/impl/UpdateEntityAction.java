package io.github.blyznytsiaorg.bibernate.actionqueue.impl;

import io.github.blyznytsiaorg.bibernate.actionqueue.ActionType;
import io.github.blyznytsiaorg.bibernate.actionqueue.EntityAction;
import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;
import io.github.blyznytsiaorg.bibernate.session.BibernateSession;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

import static io.github.blyznytsiaorg.bibernate.actionqueue.ActionType.UPDATE;

/**
 * Represents an entity action for updating entities in the Bibernate framework. This action is associated with
 * a specific entity class, a collection of entities to be updated, and a list of column snapshots representing
 * the changes to be applied during the update.
 * <p>
 * Upon execution, the action updates each entity in the collection using the provided column snapshots.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Builder
@RequiredArgsConstructor
public class UpdateEntityAction implements EntityAction {

    /**
     * The Bibernate session associated with the UpdateEntityAction, providing access to the underlying data access
     * operations and the execution context for the update action.
     */
    private final BibernateSession bibernateSession;
    /**
     * The Class object representing the type of entities to be updated.
     */
    @Getter
    private final Class<?> entityClass;
    /**
     * A collection of entities to be updated.
     */
    private final Collection<Object> entities;
    /**
     * A list of column snapshots representing the changes to be applied during the update.
     */
    private final List<ColumnSnapshot> diff;

    /**
     * Executes the entity action by updating each entity in the collection using the provided column snapshots.
     */
    @Override
    public void execute() {
        entities.forEach(entity -> bibernateSession.getDao().update(entityClass, entity, diff));
    }

    /**
     * Gets the collection of entities to be updated.
     *
     * @return A collection of entities associated with the update action.
     */
    @Override
    public Collection<?> getEntities() {
        return entities;
    }

    /**
     * Gets the type of action, which is {@code UPDATE}.
     *
     * @return The ActionType representing the type of entity action.
     */
    @Override
    public ActionType getActionType() {
        return UPDATE;
    }
}
