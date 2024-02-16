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

    private final BibernateSession bibernateSession;
    private final ActionQueue actionQueue;
    private final Map<EntityKey<?>, Object> firstLevelCache = new HashMap<>();
    private final Map<EntityKey<?>, List<ColumnSnapshot>> snapshots = new HashMap<>();

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

    @Override
    public <T> List<T> findAll(Class<T> entityClass) {
        flush();
        var entities = bibernateSession.findAll(entityClass);
        persistentContext(entityClass, entities);

        return entities;
    }

    @Override
    public <T> List<T> findAllById(Class<T> entityClass, Collection<Object> primaryKeys) {
        flush();
        var entities = bibernateSession.findAllById(entityClass, primaryKeys);
        persistentContext(entityClass, entities);

        return entities;
    }

    @Override
    public <T> List<T> findAllByColumnValue(Class<T> entityClass, String columnName, Object columnValue) {
        flush();
        var entities = bibernateSession.findAllByColumnValue(entityClass, columnName, columnValue);
        persistentContext(entityClass, entities);

        return entities;
    }

    @Override
    public <T> List<T> findByWhere(Class<T> entityClass, String whereQuery, Object[] bindValues) {
        flush();
        var entities = bibernateSession.findByWhere(entityClass, whereQuery, bindValues);
        persistentContext(entityClass, entities);

        return entities;
    }

    @Override
    public <T> List<T> findByJoinTableField(Class<T> entityClass, Field field, Object... bindValues) {
        flush();
        return bibernateSession.findByJoinTableField(entityClass, field, bindValues);
    }

    @Override
    public <T> Optional<T> findByWhereJoin(Class<T> entityClass, Object[] bindValues) {
        return bibernateSession.findByWhereJoin(entityClass, bindValues);
    }

    @Override
    public <T> List<T> findByQuery(Class<T> entityClass, String query, Object[] bindValues) {
        flush();
        var entities = bibernateSession.findByQuery(entityClass, query, bindValues);
        persistentContext(entityClass, entities);

        return entities;
    }

    @Override
    public <T> void update(Class<T> entityClass, Object entity) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL);

        this.update(entityClass, entity, List.of());
    }

    @Override
    public int find(String query, Object[] bindValues) {
        flush();
        return bibernateSession.find(query, bindValues);
    }

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

    @Override
    public void flush() {
        performDirtyChecking();
        actionQueue.executeEntityAction();
        bibernateSession.flush();
    }

    @Override
    public void close() {
        log.trace(SESSION_IS_CLOSING_PERFORMING_DIRTY_CHECKING);
        performDirtyChecking();
        actionQueue.executeEntityAction();
        resetBibernateSession();

        clearCacheAndSnapshots();

        bibernateSession.close();
    }

    @Override
    public Dao getDao() {
        return bibernateSession.getDao();
    }

    @Override
    public void startTransaction() throws SQLException {
        bibernateSession.startTransaction();
    }

    @Override
    public void commitTransaction() throws SQLException {
        bibernateSession.commitTransaction();
    }

    @Override
    public void rollbackTransaction() throws SQLException {
        bibernateSession.rollbackTransaction();
    }

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

    private void clearCacheAndSnapshots() {
        log.trace(FIRST_LEVEL_CACHE_IS_CLEARING);
        firstLevelCache.clear();

        log.trace(SNAPSHOTS_ARE_CLEARING);
        snapshots.clear();
    }

    private void removeCacheAndSnapshotBy(EntityKey<?> entityKey) {
        if (Objects.nonNull(firstLevelCache.remove(entityKey))) {
            log.trace(DELETED_ENTITY_CLASS_WITH_PRIMARY_KEY_FROM_FIRST_LEVEL_CACHE, entityKey.getClass(), entityKey.id());
        }
        if (Objects.nonNull(snapshots.remove(entityKey))) {
            log.trace(DELETED_ENTITY_CLASS_WITH_PRIMARY_KEY_FROM_SNAPSHOT, entityKey.getClass(), entityKey.id());
        }
    }

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

    private <T> EntityKey<T> prepareEntityKey(Class<T> entityClass, Object primaryKey) {
        var fieldIdType = columnIdType(entityClass);

        return new EntityKey<>(entityClass, primaryKey, fieldIdType);
    }

    private void addToQueueOrExecute(Runnable addToQueue, Runnable execute) {
        if (actionQueue.isNotExecuted()) {
            addToQueue.run();
        } else {
            execute.run();
        }
    }
}
