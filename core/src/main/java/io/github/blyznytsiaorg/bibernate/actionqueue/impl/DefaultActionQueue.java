package io.github.blyznytsiaorg.bibernate.actionqueue.impl;

import io.github.blyznytsiaorg.bibernate.actionqueue.ActionQueue;
import io.github.blyznytsiaorg.bibernate.actionqueue.ActionType;
import io.github.blyznytsiaorg.bibernate.actionqueue.EntityAction;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.exception.UnsupportedActionTypeException;
import io.github.blyznytsiaorg.bibernate.utils.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;

import static io.github.blyznytsiaorg.bibernate.actionqueue.ActionType.*;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.UNSUPPORTED_ACTION_TYPE;

/**
 * Default implementation of the {@link io.github.blyznytsiaorg.bibernate.actionqueue.ActionQueue} interface.
 * Manages and executes entity actions categorized by their types, including insert, update, and delete operations.
 * This class ensures proper handling of interdependencies between different types of actions during execution.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class DefaultActionQueue implements ActionQueue {

    /**
     * A map that categorizes entity actions based on their types (INSERT, UPDATE, DELETE).
     */
    private final Map<ActionType, Set<EntityAction>> entityActions = new HashMap<>();

    /**
     * Flag indicating whether the action queue has been executed.
     * When 'true', it signifies that the action queue should not perform any
     * execution of entity actions, providing control over the processing flow.
     */
    private boolean isExecuted;

    /**
     * Executes entity actions in the order of INSERT, UPDATE, and DELETE types.
     * Clears the entity actions map after execution.
     */
    @Override
    public void executeEntityAction() {
        if (isNotExecuted()) {
            isExecuted = true;
            executeIfNotEmpty(this::executeInsertEntityAction, entityActions.get(INSERT));
            executeIfNotEmpty(this::executeUpdateEntityAction, entityActions.get(UPDATE));
            executeIfNotEmpty(this::executeDeleteEntityAction, entityActions.get(DELETE));

            entityActions.clear();
            isExecuted = false;
        }
    }

    /**
     * Adds an entity action to the corresponding type category in the entity actions map.
     *
     * @param entityAction The entity action to be added.
     */
    @Override
    public void addEntityAction(EntityAction entityAction) {
        initEntityActions();
        switch (entityAction.getActionType()) {
            case INSERT -> addInsertAction(entityAction);
            case UPDATE -> addUpdateAction(entityAction);
            case DELETE -> addDeleteAction(entityAction);
            default -> throw new UnsupportedActionTypeException(
                    UNSUPPORTED_ACTION_TYPE.formatted(entityAction.getActionType()));
        }
    }

    /**
     * Checks if the action queue is set to not execute actions. When this method returns true,
     * it indicates that the action queue is in a state where it should not perform any execution
     * of entity actions.
     *
     * @return true if the action queue is set to not execute actions, false otherwise.
     */
    @Override
    public boolean isNotExecuted() {
        return !isExecuted;
    }

    /**
     * Initializes the entity actions map to ensure each type category has an associated set.
     */
    private void initEntityActions() {
        this.entityActions.computeIfAbsent(INSERT, k -> new LinkedHashSet<>());
        this.entityActions.computeIfAbsent(UPDATE, k -> new LinkedHashSet<>());
        this.entityActions.computeIfAbsent(DELETE, k -> new LinkedHashSet<>());
    }

    /**
     * Adds an INSERT type entity action to the entity actions map, taking into account interdependencies
     * with UPDATE and DELETE type actions.
     *
     * @param insertEntityAction The INSERT type entity action to be added.
     */
    private void addInsertAction(EntityAction insertEntityAction) {
        entityActions.get(UPDATE).stream()
                .filter(updateAction -> updateAction.getEntityClass().equals(insertEntityAction.getEntityClass()))
                .forEach(updateAction -> updateAction.getEntities()
                        .removeIf(updateEntity -> insertEntityAction.getEntities().contains(updateEntity)));

        entityActions.get(DELETE).stream()
                .filter(deleteAction -> deleteAction.getEntityClass().equals(insertEntityAction.getEntityClass()))
                .flatMap(deleteAction -> deleteAction.getEntities().stream())
                .forEach(deleteEntity -> insertEntityAction.getEntities()
                        .removeIf(insertEntity -> insertEntity.equals(deleteEntity)));

        entityActions.get(INSERT).add(insertEntityAction);
    }

    /**
     * Adds an UPDATE type entity action to the entity actions map, taking into account interdependencies
     * with INSERT and DELETE type actions.
     *
     * @param updateEntityAction The UPDATE type entity action to be added.
     */
    private void addUpdateAction(EntityAction updateEntityAction) {
        entityActions.get(INSERT).stream()
                .filter(insertAction -> insertAction.getEntityClass().equals(updateEntityAction.getEntityClass()))
                .flatMap(insertAction -> insertAction.getEntities().stream())
                .forEach(insertEntity -> updateEntityAction.getEntities()
                        .removeIf(updateEntity -> updateEntity.equals(insertEntity)));

        entityActions.get(DELETE).stream()
                .filter(deleteAction -> deleteAction.getEntityClass().equals(updateEntityAction.getEntityClass()))
                .flatMap(deleteAction -> deleteAction.getEntities().stream())
                .forEach(deleteEntity -> updateEntityAction.getEntities()
                        .removeIf(updateEntity -> updateEntity.equals(deleteEntity)));

        entityActions.get(UPDATE).add(updateEntityAction);
    }

    /**
     * Adds a DELETE type entity action to the entity actions map, taking into account interdependencies
     * with INSERT and UPDATE type actions.
     *
     * @param deleteEntityAction The DELETE type entity action to be added.
     */
    private void addDeleteAction(EntityAction deleteEntityAction) {
        entityActions.get(INSERT).stream()
                .filter(insertAction -> insertAction.getEntityClass().equals(deleteEntityAction.getEntityClass()))
                .forEach(insertAction -> insertAction.getEntities()
                        .removeIf(insertEntity -> deleteEntityAction.getEntities().contains(insertEntity)));

        entityActions.get(UPDATE).stream()
                .filter(updateAction -> updateAction.getEntityClass().equals(deleteEntityAction.getEntityClass()))
                .forEach(updateAction -> updateAction.getEntities()
                        .removeIf(updateEntity -> deleteEntityAction.getEntities().contains(updateEntity)));

        entityActions.get(DELETE).add(deleteEntityAction);
    }

    /**
     * Executes INSERT type entity actions.
     *
     * @param insertEntityActions The set of INSERT type entity actions to be executed.
     */
    private void executeInsertEntityAction(Set<EntityAction> insertEntityActions) {
        insertEntityActions.forEach(EntityAction::execute);
    }

    /**
     * Executes UPDATE type entity actions.
     *
     * @param updateEntityActions The set of UPDATE type entity actions to be executed.
     */
    private void executeUpdateEntityAction(Set<EntityAction> updateEntityActions) {
        updateEntityActions.forEach(EntityAction::execute);
    }

    /**
     * Executes DELETE type entity actions.
     *
     * @param deleteEntityActions The set of DELETE type entity actions to be executed.
     */
    private void executeDeleteEntityAction(Set<EntityAction> deleteEntityActions) {
        deleteEntityActions.forEach(EntityAction::execute);
    }

    /**
     * Executes a consumer on a collection if the collection is not empty.
     *
     * @param consumer    The consumer to be executed.
     * @param collections The collection to be checked and passed to the consumer if not empty.
     * @param <T>         The type of the collection.
     */
    private <T extends Collection<?>> void executeIfNotEmpty(Consumer<T> consumer, T collections) {
        if (CollectionUtils.isNotEmpty(collections)) {
            consumer.accept(collections);
        }
    }
}
