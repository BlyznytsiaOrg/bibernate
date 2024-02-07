package io.github.blyznytsiaorg.bibernate.actionqueue.impl;

import io.github.blyznytsiaorg.bibernate.actionqueue.ActionType;
import io.github.blyznytsiaorg.bibernate.actionqueue.EntityAction;
import io.github.blyznytsiaorg.bibernate.session.BibernateSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
public class InsertEntityAction extends EntityAction {

    private final BibernateSession bibernateSession;
    @Getter
    private final Class<?> entityClass;
    private final Object entity;

    @Override
    public void execute() {
        bibernateSession.save(entityClass, entity);
    }

    @Override
    public ActionType getActionType() {
        return ActionType.INSERT;
    }
}
