package io.github.blyznytsiaorg.bibernate.actionqueue.impl;

import io.github.blyznytsiaorg.bibernate.actionqueue.ActionType;
import io.github.blyznytsiaorg.bibernate.actionqueue.EntityAction;
import io.github.blyznytsiaorg.bibernate.session.BibernateSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static io.github.blyznytsiaorg.bibernate.actionqueue.ActionType.DELETE;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
public class DeleteEntityAction extends EntityAction {

    private final BibernateSession bibernateSession;
    @Getter
    private final Class<?> entityClass;
    private final Object entity;

    @Override
    public void execute() {
        bibernateSession.delete(entityClass, entity);
    }

    @Override
    public ActionType getActionType() {
        return DELETE;
    }
}
