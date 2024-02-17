package io.github.blyznytsiaorg.bibernate.session;

import io.github.blyznytsiaorg.bibernate.actionqueue.ActionQueue;
import io.github.blyznytsiaorg.bibernate.actionqueue.EntityAction;
import io.github.blyznytsiaorg.bibernate.actionqueue.impl.*;
import io.github.blyznytsiaorg.bibernate.annotation.OneToOne;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;
import io.github.blyznytsiaorg.bibernate.dao.Dao;
import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;
import io.github.blyznytsiaorg.bibernate.entity.EntityKey;
import io.github.blyznytsiaorg.bibernate.utils.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder.resetBibernateSession;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.*;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.*;

/**
 * BibernateFirstLevelCacheSession is an implementation of the BibernateSession interface
 * that introduces a first-level cache to improve the performance of entity retrieval operations.
 * It wraps an existing BibernateSession and maintains a first-level cache along with snapshots
 * for dirty checking. This class intercepts various session operations, manages the cache,
 * and delegates the actual database interactions to the underlying BibernateSession.
 * <p>
 * The first-level cache stores entities in-memory, allowing for quick retrieval without repeated
 * database queries. Snapshots are used for dirty checking, identifying changes in entities and
 * triggering necessary updates to the database.
 * <p>
 * This class is part of the Bibernate framework and supports common CRUD operations, query
 * executions, and transaction management.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class BibernateFirstLevelCacheSession implements BibernateSession {

    /**
     * The underlying Bibernate session that this first-level cache session wraps.
     */
    private final BibernateSession bibernateSession;

    /**
     * The action queue for managing and executing entity actions.
     */
    private final ActionQueue actionQueue;

    /**
     * The first-level cache that holds entities in-memory for quick access.
     * The key is the EntityKey, and the value is the corresponding entity.
     */
    private final Map<EntityKey<?>, Object> firstLevelCache = new HashMap<>();

    /**
     * Snapshots of entities stored in the first-level cache.
     * The key is the EntityKey, and the value is a list of ColumnSnapshots representing the entity's state.
     */
    private final Map<EntityKey<?>, List<ColumnSnapshot>> snapshots = new HashMap<>();

    /**
     * Retrieves an entity by its primary key from the first-level cache or delegates to
     * the underlying BibernateSession to fetch it from the database.
     *
     * @param entityClass The class of the entity to retrieve.
     * @param primaryKey  The primary key of the entity to retrieve.
     * @param <T>         The type of the entity.
     * @return An Optional containing the retrieved entity or empty if not found.
     */
    @Override
    public <T> Optional<T> findById(Class<T> entityClass, Object primaryKey) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(primaryKey, PRIMARY_KEY_MUST_BE_NOT_NULL);

        flush();
        var fieldIdType = columnIdType(entityClass);
        primaryKey = castIdToEntityId(entityClass, primaryKey);
        var entityKey = new EntityKey<>(entityClass, primaryKey, fieldIdType);
        var cachedEntity = firstLevelCache.get(entityKey);

        if (Objects.isNull(cachedEntity)) {
            var finalPrimaryKey = primaryKey;
            log.trace(ENTITY_NOT_FOUND_IN_FIRST_LEVEL_CACHE_BY_ID, entityClass.getSimpleName(), finalPrimaryKey);

            return bibernateSession.findById(entityClass, primaryKey)
                    .map(entityFromDb -> persistentContext(entityClass, entityFromDb, entityKey, finalPrimaryKey));
        }

        log.trace(ENTITY_FOUND_IN_FIRST_LEVEL_CACHE_BY_ID, entityClass.getSimpleName(), primaryKey);

        return Optional.of(entityClass.cast(cachedEntity));
    }

    /**
     * Retrieves all entities of a given class from the first-level cache or delegates to
     * the underlying BibernateSession to fetch them from the database.
     *
     * @param entityClass The class of the entities to retrieve.
     * @param <T>         The type of the entities.
     * @return A list of retrieved entities.
     */
    @Override
    public <T> List<T> findAll(Class<T> entityClass) {
        flush();
        var entities = bibernateSession.findAll(entityClass);
        persistentContext(entityClass, entities);

        return entities;
    }

    /**
     * Retrieves all entities of a given class by their primary keys from the first-level
     * cache or delegates to the underlying BibernateSession to fetch them from the database.
     *
     * @param entityClass The class of the entities to retrieve.
     * @param primaryKeys The primary keys of the entities to retrieve.
     * @param <T>         The type of the entities.
     * @return A list of retrieved entities.
     */
    @Override
    public <T> List<T> findAllById(Class<T> entityClass, Collection<Object> primaryKeys) {
        flush();
        var entities = bibernateSession.findAllById(entityClass, primaryKeys);
        persistentContext(entityClass, entities);

        return entities;
    }

    /**
     * Retrieves entities of a given class by a specified column value from the first-level
     * cache or delegates to the underlying BibernateSession to fetch them from the database.
     *
     * @param entityClass The class of the entities to retrieve.
     * @param columnName  The name of the column to match.
     * @param columnValue The value to match in the specified column.
     * @param <T>         The type of the entities.
     * @return A list of retrieved entities.
     */
    @Override
    public <T> List<T> findAllByColumnValue(Class<T> entityClass, String columnName, Object columnValue) {
        flush();
        var entities = bibernateSession.findAllByColumnValue(entityClass, columnName, columnValue);
        persistentContext(entityClass, entities);

        return entities;
    }

    /**
     * Retrieves entities of a given class by a specified WHERE query from the first-level
     * cache or delegates to the underlying BibernateSession to fetch them from the database.
     *
     * @param entityClass The class of the entities to retrieve.
     * @param whereQuery  The WHERE query.
     * @param bindValues  The values to bind to the query.
     * @param <T>         The type of the entities.
     * @return A list of retrieved entities.
     */
    @Override
    public <T> List<T> findByWhere(Class<T> entityClass, String whereQuery, Object[] bindValues) {
        flush();
        var entities = bibernateSession.findByWhere(entityClass, whereQuery, bindValues);
        persistentContext(entityClass, entities);

        return entities;
    }

    /**
     * Retrieves entities of a given class by joining with a specified field from the
     * first-level cache or delegates to the underlying BibernateSession to fetch them
     * from the database.
     *
     * @param entityClass The class of the entities to retrieve.
     * @param field       The field to join on.
     * @param bindValues  The values to bind to the query.
     * @param <T>         The type of the entities.
     * @return A list of retrieved entities.
     */
    @Override
    public <T> List<T> findByJoinTableField(Class<T> entityClass, Field field, Object... bindValues) {
        flush();
        return bibernateSession.findByJoinTableField(entityClass, field, bindValues);
    }

    /**
     * Retrieves an entity of a given class by a specified WHERE JOIN query from the
     * first-level cache or delegates to the underlying BibernateSession to fetch it
     * from the database.
     *
     * @param entityClass The class of the entity to retrieve.
     * @param bindValues  The values to bind to the query.
     * @param <T>         The type of the entity.
     * @return An Optional containing the retrieved entity or empty if not found.
     */
    @Override
    public <T> Optional<T> findByWhereJoin(Class<T> entityClass, Object[] bindValues) {
        return bibernateSession.findByWhereJoin(entityClass, bindValues);
    }

    /**
     * Retrieves entities of a given class by executing a specified query from the
     * first-level cache or delegates to the underlying BibernateSession to fetch them
     * from the database.
     *
     * @param entityClass The class of the entities to retrieve.
     * @param query       The query to execute.
     * @param bindValues  The values to bind to the query.
     * @param <T>         The type of the entities.
     * @return A list of retrieved entities.
     */
    @Override
    public <T> List<T> findByQuery(Class<T> entityClass, String query, Object[] bindValues) {
        flush();
        var entities = bibernateSession.findByQuery(entityClass, query, bindValues);
        persistentContext(entityClass, entities);

        return entities;
    }

    /**
     * Updates an entity of a given class in the first-level cache and queues an update
     * action to be executed or executes it immediately if no pending actions are present.
     *
     * @param entityClass The class of the entity to update.
     * @param entity      The entity to update.
     * @param <T>         The type of the entity.
     */
    @Override
    public <T> void update(Class<T> entityClass, Object entity) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL);

        this.update(entityClass, entity, List.of());
    }

    /**
     * Executes a find query with the specified parameters and returns the count of affected rows.
     * This method flushes the cache before executing the query.
     *
     * @param query      The find query to execute.
     * @param bindValues The values to bind to the query.
     * @return The count of affected rows.
     */
    @Override
    public int find(String query, Object[] bindValues) {
        flush();
        return bibernateSession.find(query, bindValues);
    }

    /**
     * Saves an entity of a given class in the first-level cache and queues an insert action
     * to be executed or executes it immediately if no pending actions are present.
     *
     * @param entityClass The class of the entity to save.
     * @param entity      The entity to save.
     * @param <T>         The type of the entity.
     * @return The saved entity.
     */
    @Override
    public <T> T save(Class<T> entityClass, T entity) {
        addToQueueOrExecute(
                () -> actionQueue.addEntityAction(InsertEntityAction.<T>builder()
                        .bibernateSession(bibernateSession)
                        .entityClass(entityClass)
                        .entities(new HashSet<>(Set.of(entity)))
                        .build()),
                () -> bibernateSession.save(entityClass, entity));

        return entityClass.cast(entity);
    }

    /**
     * Saves a collection of entities of a given class in the first-level cache and queues
     * an insert action to be executed or executes it immediately if no pending actions are present.
     *
     * @param entityClass The class of the entities to save.
     * @param entities    The entities to save.
     * @param <T>         The type of the entities.
     */
    @Override
    public <T> void saveAll(Class<T> entityClass, Collection<T> entities) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        CollectionUtils.requireNonEmpty(entities, COLLECTION_MUST_BE_NOT_EMPTY);

        addToQueueOrExecute(
                () -> actionQueue.addEntityAction(InsertAllEntityAction.<T>builder()
                        .bibernateSession(bibernateSession)
                        .entityClass(entityClass)
                        .entities(new LinkedHashSet<>(entities))
                        .build()),
                () -> bibernateSession.saveAll(entityClass, entities));
    }

    /**
     * Deletes an entity of a given class by its primary key in the first-level cache and
     * queues a delete action to be executed or executes it immediately if no pending actions
     * are present.
     *
     * @param entityClass The class of the entity to delete.
     * @param primaryKey  The primary key of the entity to delete.
     */
    @Override
    public <T> void deleteById(Class<T> entityClass, Object primaryKey) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(primaryKey, PRIMARY_KEY_MUST_BE_NOT_NULL);

        var finalPrimaryKey = castIdToEntityId(entityClass, primaryKey);
        var entityKey = prepareEntityKey(entityClass, finalPrimaryKey);

        addToQueueOrExecute(
                () -> actionQueue.addEntityAction(
                        prepareDeleteByIdEntityAction(entityClass, finalPrimaryKey, entityKey)),
                () -> {
                    bibernateSession.deleteById(entityClass, finalPrimaryKey);
                    removeCacheAndSnapshotBy(entityKey);
                });
    }

    /**
     * Deletes entities of a given class by their primary keys in the first-level cache and
     * queues a delete action to be executed or executes it immediately if no pending actions
     * are present.
     *
     * @param entityClass The class of the entities to delete.
     * @param primaryKeys The primary keys of the entities to delete.
     */
    @Override
    public <T> void deleteAllById(Class<T> entityClass, Collection<Object> primaryKeys) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        CollectionUtils.requireNonEmpty(primaryKeys, COLLECTION_MUST_BE_NOT_EMPTY);

        Collection<Object> finalPrimaryKeys = primaryKeys.stream()
                .map(id -> castIdToEntityId(entityClass, id))
                .collect(Collectors.toList());
        var entityKeys = finalPrimaryKeys.stream()
                .map(id -> prepareEntityKey(entityClass, id))
                .toList();

        addToQueueOrExecute(
                () -> actionQueue.addEntityAction(DeleteAllByIdEntityAction.<T>builder()
                        .bibernateSession(bibernateSession)
                        .entityClass(entityClass)
                        .primaryKeys(finalPrimaryKeys)
                        .entities(bibernateSession.findAllById(entityClass, finalPrimaryKeys))
                        .removeCacheAndSnapshot(() -> removeCacheAndSnapshotBy(entityKeys, entityClass, finalPrimaryKeys))
                        .build()),
                () -> {
                    bibernateSession.deleteAllById(entityClass, finalPrimaryKeys);
                    removeCacheAndSnapshotBy(entityKeys, entityClass, finalPrimaryKeys);
                });
    }

    /**
     * Deletes entities of a given class by a specified column value in the first-level cache and
     * delegates to the underlying BibernateSession to delete them from the database.
     *
     * @param entityClass The class of the entities to delete.
     * @param columnName  The name of the column to match.
     * @param columnValue The value to match in the specified column.
     * @param <T>         The type of the entities.
     * @return A list of deleted entities.
     */
    @Override
    public <T> List<T> deleteByColumnValue(Class<T> entityClass, String columnName, Object columnValue) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(columnName, FIELD_MUST_BE_NOT_NULL);

        var deletedEntities = bibernateSession.deleteByColumnValue(entityClass, columnName, columnValue);

        var entityKeys = deletedEntities.stream()
                .map(entity -> prepareEntityKey(entityClass, getValueFromObject(entity, getIdField(entityClass))))
                .toList();
        var primaryKeys = entityKeys.stream()
                .map(EntityKey::id)
                .toList();

        removeCacheAndSnapshotBy(entityKeys, entityClass, primaryKeys);

        return deletedEntities;
    }

    /**
     * Deletes an entity of a given class in the first-level cache and
     * queues a delete action to be executed or executes it immediately if no pending actions
     * are present.
     *
     * @param entityClass The class of the entity to delete.
     * @param entity      The entity to delete.
     * @param <T>         The type of the entity.
     */
    @Override
    public <T> void delete(Class<T> entityClass, T entity) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL);

        var primaryKey = columnIdValue(entityClass, entity);
        var entityKey = prepareEntityKey(entityClass, primaryKey);

        addToQueueOrExecute(
                () -> actionQueue.addEntityAction(
                        DeleteEntityAction.<T>builder()
                                .bibernateSession(bibernateSession)
                                .entityClass(entityClass)
                                .entities(new HashSet<>(Set.of(entity)))
                                .removeCacheAndSnapshot(() -> removeCacheAndSnapshotBy(entityKey))
                                .build()),
                () -> {
                    bibernateSession.delete(entityClass, entity);
                    removeCacheAndSnapshotBy(entityKey);
                });
    }

    /**
     * Deletes entities of a given class in the first-level cache and
     * queues a delete action to be executed or executes it immediately if no pending actions
     * are present.
     *
     * @param entityClass The class of the entities to delete.
     * @param entities    The entities to delete.
     * @param <T>         The type of the entities.
     */
    @Override
    public <T> void deleteAll(Class<T> entityClass, Collection<T> entities) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        CollectionUtils.requireNonEmpty(entities, COLLECTION_MUST_BE_NOT_EMPTY);

        var entityKeys = entities.stream()
                .map(entity -> prepareEntityKey(entityClass, getValueFromObject(entity, getIdField(entityClass))))
                .toList();
        var primaryKeys = entityKeys.stream()
                .map(EntityKey::id)
                .toList();

        addToQueueOrExecute(
                () -> actionQueue.addEntityAction(DeleteAllEntityAction.<T>builder()
                        .bibernateSession(bibernateSession)
                        .entityClass(entityClass)
                        .entities(entities)
                        .removeCacheAndSnapshot(() -> removeCacheAndSnapshotBy(entityKeys, entityClass, primaryKeys))
                        .build()),
                () -> {
                    bibernateSession.deleteAll(entityClass, entities);
                    removeCacheAndSnapshotBy(entityKeys, entityClass, primaryKeys);
                });
    }

    /**
     * Flushes the session by performing dirty checking, executing pending actions,
     * and delegating to the underlying BibernateSession.
     */
    @Override
    public void flush() {
        performDirtyChecking();
        actionQueue.executeEntityAction();
        bibernateSession.flush();
    }

    /**
     * Closes the session by performing dirty checking, executing pending actions,
     * and resetting the Bibernate session. Clears the first-level cache and snapshots.
     */
    @Override
    public void close() {
        log.trace(SESSION_IS_CLOSING_PERFORMING_DIRTY_CHECKING);
        performDirtyChecking();
        actionQueue.executeEntityAction();
        resetBibernateSession();

        clearCacheAndSnapshots();

        bibernateSession.close();
    }

    /**
     * Retrieves the Dao associated with the session.
     *
     * @return The Dao associated with the session.
     */
    @Override
    public Dao getDao() {
        return bibernateSession.getDao();
    }

    /**
     * Starts a transaction using the underlying BibernateSession.
     *
     * @throws SQLException If a SQL exception occurs while starting the transaction.
     */
    @Override
    public void startTransaction() throws SQLException {
        bibernateSession.startTransaction();
    }

    /**
     * Commits the current transaction using the underlying BibernateSession.
     *
     * @throws SQLException If a SQL exception occurs while committing the transaction.
     */
    @Override
    public void commitTransaction() throws SQLException {
        bibernateSession.commitTransaction();
    }

    /**
     * Rolls back the current transaction using the underlying BibernateSession.
     *
     * @throws SQLException If a SQL exception occurs while rolling back the transaction.
     */
    @Override
    public void rollbackTransaction() throws SQLException {
        bibernateSession.rollbackTransaction();
    }

    /**
     * Builds a snapshot of an entity by iterating through its fields and creating
     * ColumnSnapshot instances. Used for dirty checking.
     *
     * @param entity The entity for which to build a snapshot.
     * @return A list of ColumnSnapshot instances representing the entity's state.
     */
    private List<ColumnSnapshot> buildEntitySnapshot(Object entity) {
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL);

        var entityClass = entity.getClass();
        var declaredFields = entityClass.getDeclaredFields();
        var snapshot = new ArrayList<ColumnSnapshot>(declaredFields.length);

        for (var field : declaredFields) {
            var value = getValueFromObject(entity, field);
            var columnName = columnName(field);
            snapshot.add(new ColumnSnapshot(columnName, value, field.getType()));
        }

        return snapshot;
    }

    /**
     * Updates an entity of a given class in the first-level cache and queues an update
     * action to be executed or executes it immediately if no pending actions are present.
     * Used for updating entities and triggering dirty checking.
     *
     * @param entityClass The class of the entity to update.
     * @param entity      The entity to update.
     * @param diff        The list of differences between the entity and its snapshot.
     * @param <T>         The type of the entity.
     */
    private <T> void update(Class<T> entityClass, Object entity, List<ColumnSnapshot> diff) {
        addToQueueOrExecute(
                () -> actionQueue.addEntityAction(UpdateEntityAction.builder()
                        .bibernateSession(bibernateSession)
                        .entityClass(entityClass)
                        .entities(new HashSet<>(Set.of(entity)))
                        .diff(diff)
                        .build()),
                () -> bibernateSession.getDao().update(entityClass, entity, diff));
    }

    /**
     * Performs dirty checking by comparing the current state of entities in the first-level
     * cache with their snapshots. If differences are detected, corresponding update actions
     * are queued for execution.
     */
    private void performDirtyChecking() {
        firstLevelCache.keySet().forEach(entityKey -> {
            var entityInFirstLevelCache = firstLevelCache.get(entityKey);
            var entityInFirstLevelCacheCurrentSnapshot = buildEntitySnapshot(entityInFirstLevelCache);
            var entityOldSnapshot = snapshots.get(entityKey);
            var diff = getDifference(entityInFirstLevelCacheCurrentSnapshot, entityOldSnapshot);

            if (CollectionUtils.isNotEmpty(diff)) {
                log.trace(DIRTY_ENTITY_FOUND_NEED_TO_GENERATE_UPDATE_FOR_ENTITY_KEY_AND_ENTITY,
                        entityKey, entityInFirstLevelCache);
                update(entityInFirstLevelCache.getClass(), entityInFirstLevelCache, diff);
            } else {
                log.trace(DIRTY_ENTITY_NOT_FOUND_FOR_ENTITY_KEY_NO_CHANGES, entityKey);
            }
        });
    }

    /**
     * Prepares a DeleteByIdEntityAction for the specified entity class, primary key, and EntityKey.
     * Retrieves the entity from the cache if available; otherwise, fetches it from the underlying BibernateSession.
     * The action includes the removal of the entity from the first-level cache and snapshots.
     *
     * @param <T>         The type of the entity.
     * @param entityClass The class of the entity.
     * @param primaryKey  The primary key of the entity.
     * @param entityKey   The EntityKey representing the entity.
     * @return The prepared DeleteByIdEntityAction.
     */
    private <T> EntityAction prepareDeleteByIdEntityAction(Class<T> entityClass,
                                                           Object primaryKey,
                                                           EntityKey<T> entityKey) {
        var entities = findById(entityClass, primaryKey)
                .map(entity -> new HashSet<>(Set.of(entity)))
                .orElse(new HashSet<>());

        return DeleteByIdEntityAction.<T>builder()
                .bibernateSession(bibernateSession)
                .entityClass(entityClass)
                .primaryKey(primaryKey)
                .entities(entities)
                .removeCacheAndSnapshot(() -> removeCacheAndSnapshotBy(entityKey))
                .build();
    }

    /**
     * Persists the context for a list of entities in the first-level cache.
     * Checks each entity against the cache, updating it with the cached instance if present,
     * or adds it to the cache and snapshots otherwise.
     *
     * @param <T>         The type of the entity.
     * @param entityClass The class of the entity.
     * @param entities    The list of entities to persist in the context.
     */
    private <T> void persistentContext(Class<T> entityClass, List<T> entities) {
        for (int i = 0; i < entities.size(); i++) {
            var entityFromDb = entities.get(i);
            var fieldIdType = columnIdType(entityClass);
            var fieldIdValue = columnIdValue(entityClass, entityFromDb);
            var entityKey = new EntityKey<>(entityClass, fieldIdValue, fieldIdType);
            var entityCached = firstLevelCache.get(entityKey);

            if (Objects.nonNull(entityCached)) {
                entities.set(i, entityClass.cast(entityCached));
            } else {
                persistentContext(entityClass, entityFromDb, entityKey, fieldIdValue);
            }
        }
    }

    /**
     * Persists the context for a single entity in the first-level cache.
     * Checks if the entity has lazy fields and, if not, adds it to the cache and creates a snapshot.
     * The entity is added to the cache using its EntityKey as the key.
     *
     * @param <T>             The type of the entity.
     * @param entityClass     The class of the entity.
     * @param entityFromDb    The entity to persist in the context.
     * @param entityKey       The EntityKey representing the entity.
     * @param finalPrimaryKey The final primary key value.
     * @return The persistent entity.
     */
    private <T> T persistentContext(Class<?> entityClass, T entityFromDb, EntityKey<?> entityKey,
                                    Object finalPrimaryKey) {
        if (!isImmutable(entityClass)) {
            var isEntityHasLazyField = Arrays.stream(entityFromDb.getClass().getDeclaredFields())
                    .anyMatch(field -> field.isAnnotationPresent(OneToOne.class) &&
                                       field.getAnnotation(OneToOne.class).fetch() == FetchType.LAZY);

            if (!isEntityHasLazyField) {
                firstLevelCache.put(entityKey, entityFromDb);
                List<ColumnSnapshot> entityCurrentSnapshot = buildEntitySnapshot(entityFromDb);
                snapshots.put(entityKey, entityCurrentSnapshot);
                log.trace(CREATED_SNAPSHOT_FOR_ENTITY_ID, entityClass.getSimpleName(), finalPrimaryKey);
            }
        }

        return entityFromDb;
    }

    /**
     * Clears both the first-level cache and snapshots, removing all entities and their snapshots.
     */
    private void clearCacheAndSnapshots() {
        log.trace(FIRST_LEVEL_CACHE_IS_CLEARING);
        firstLevelCache.clear();

        log.trace(SNAPSHOTS_ARE_CLEARING);
        snapshots.clear();
    }

    /**
     * Removes an entity and its snapshot from the first-level cache using the provided EntityKey.
     * Logs trace messages indicating the removal from the cache and snapshots.
     *
     * @param entityKey The EntityKey representing the entity to remove.
     */
    private void removeCacheAndSnapshotBy(EntityKey<?> entityKey) {
        if (Objects.nonNull(firstLevelCache.remove(entityKey))) {
            log.trace(DELETED_ENTITY_CLASS_WITH_PRIMARY_KEY_FROM_FIRST_LEVEL_CACHE, entityKey.getClass(), entityKey.id());
        }
        if (Objects.nonNull(snapshots.remove(entityKey))) {
            log.trace(DELETED_ENTITY_CLASS_WITH_PRIMARY_KEY_FROM_SNAPSHOT, entityKey.getClass(), entityKey.id());
        }
    }

    /**
     * Removes multiple entities and their snapshots from the first-level cache using a list of EntityKeys.
     * Logs trace messages indicating the removal from the cache and snapshots.
     *
     * @param entityKeys  The list of EntityKeys representing the entities to remove.
     * @param <T>         The type of the entities.
     * @param entityClass The class of the entities.
     * @param primaryKeys The list of primary keys of the entities.
     */
    private <T> void removeCacheAndSnapshotBy(Collection<EntityKey<T>> entityKeys,
                                              Class<T> entityClass,
                                              Collection<Object> primaryKeys) {
        if (firstLevelCache.keySet().removeAll(entityKeys)) {
            log.trace(DELETED_ENTITY_CLASS_WITH_PRIMARY_KEYS_FROM_FIRST_LEVEL_CACHE, entityClass, primaryKeys);
        }
        if (snapshots.keySet().removeAll(entityKeys)) {
            log.trace(DELETED_ENTITY_CLASS_WITH_PRIMARY_KEYS_FROM_SNAPSHOT, entityClass, primaryKeys);
        }
    }

    /**
     * Prepares an EntityKey for a given entity class and primary key.
     *
     * @param <T>         The type of the entity.
     * @param entityClass The class of the entity.
     * @param primaryKey  The primary key of the entity.
     * @return The prepared EntityKey.
     */
    private <T> EntityKey<T> prepareEntityKey(Class<T> entityClass, Object primaryKey) {
        var fieldIdType = columnIdType(entityClass);

        return new EntityKey<>(entityClass, primaryKey, fieldIdType);
    }

    /**
     * Adds the specified operations to a queue for later execution or immediately executes them,
     * based on the execution state of the underlying ActionQueue.
     *
     * @param addToQueue The operation to add to the queue.
     * @param execute    The operation to execute immediately.
     */
    private void addToQueueOrExecute(Runnable addToQueue, Runnable execute) {
        if (actionQueue.isNotExecuted()) {
            addToQueue.run();
        } else {
            execute.run();
        }
    }
}
