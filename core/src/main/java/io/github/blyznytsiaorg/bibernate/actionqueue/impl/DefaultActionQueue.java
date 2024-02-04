package io.github.blyznytsiaorg.bibernate.actionqueue.impl;

import io.github.blyznytsiaorg.bibernate.actionqueue.ActionQueue;
import io.github.blyznytsiaorg.bibernate.actionqueue.ActionType;
import io.github.blyznytsiaorg.bibernate.actionqueue.EntityAction;
import io.github.blyznytsiaorg.bibernate.utils.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class DefaultActionQueue implements ActionQueue {

    private final Map<ActionType, Set<EntityAction>> entityActions = new HashMap<>();

    @Override
    public void executeEntityAction() {
        executeIfNotEmpty(this::executeInsertEntityAction, entityActions.get(ActionType.INSERT));
        executeIfNotEmpty(this::executeUpdateEntityAction, entityActions.get(ActionType.UPDATE));
        executeIfNotEmpty(this::executeDeleteEntityAction, entityActions.get(ActionType.DELETE));

        entityActions.clear();
    }

    @Override
    public void addEntityAction(EntityAction entityAction) {
        entityActions.computeIfAbsent(entityAction.getActionType(), k -> new LinkedHashSet<>())
                .add(entityAction);
    }

    private void executeInsertEntityAction(Set<EntityAction> insertEntityActions) {
        insertEntityActions.forEach(EntityAction::execute);
    }

    private void executeUpdateEntityAction(Set<EntityAction> updateEntityActions) {
        updateEntityActions.forEach(EntityAction::execute);
    }

    private void executeDeleteEntityAction(Set<EntityAction> deleteEntityActions) {
        deleteEntityActions.forEach(EntityAction::execute);
    }

    private <T extends Collection<?>> void executeIfNotEmpty(Consumer<T> consumer, T collections) {
        if (CollectionUtils.isNotEmpty(collections)) {
            consumer.accept(collections);
        }
    }
}
