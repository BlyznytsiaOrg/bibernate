package io.github.blyznytsiaorg.bibernate.actionqueue.impl;

import io.github.blyznytsiaorg.bibernate.actionqueue.ActionType;
import io.github.blyznytsiaorg.bibernate.actionqueue.EntityAction;
import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;
import io.github.blyznytsiaorg.bibernate.session.BibernateSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.actionqueue.ActionType.UPDATE;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
public class UpdateEntityAction extends EntityAction {

    private final BibernateSession bibernateSession;
    @Getter
    private final Class<?> entityClass;
    private final Object entity;
    private final List<ColumnSnapshot> diff;

    @Override
    public void execute() {
        bibernateSession.getDao().update(entityClass, entity, diff);
    }

    @Override
    public ActionType getActionType() {
        return UPDATE;
    }
}
