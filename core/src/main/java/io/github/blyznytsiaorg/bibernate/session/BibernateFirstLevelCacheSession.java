package io.github.blyznytsiaorg.bibernate.session;

import io.github.blyznytsiaorg.bibernate.actionqueue.impl.DefaultActionQueue;
import io.github.blyznytsiaorg.bibernate.actionqueue.impl.DeleteEntityAction;
import io.github.blyznytsiaorg.bibernate.actionqueue.impl.InsertEntityAction;
import io.github.blyznytsiaorg.bibernate.actionqueue.impl.UpdateEntityAction;
import io.github.blyznytsiaorg.bibernate.annotation.FetchType;
import io.github.blyznytsiaorg.bibernate.annotation.OneToOne;
import io.github.blyznytsiaorg.bibernate.dao.Dao;
import io.github.blyznytsiaorg.bibernate.entity.*;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityColumnDetails;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata;
import io.github.blyznytsiaorg.bibernate.utils.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.*;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.*;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class BibernateFirstLevelCacheSession implements BibernateSession {

    private final BibernateSession bibernateSession;
    private final DefaultActionQueue defaultActionQueue;
    private final Map<EntityKey<?>, Object> firstLevelCache = new HashMap<>();
    private final Map<EntityKey<?>, List<ColumnSnapshot>> snapshots = new HashMap<>();
    private final Map<Class<?>, EntityMetadata> bibernateEntityMetadata = BibernateEntityMetadataHolder.getBibernateEntityMetadata();

    @Override
    public <T> Optional<T> findById(Class<T> entityClass, Object primaryKey) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(primaryKey, PRIMARY_KEY_MUST_BE_NOT_NULL);

        flush();
        EntityMetadata entityMetadata = bibernateEntityMetadata.get(entityClass);
        var fieldIdType = columnIdType(entityClass);
        primaryKey = castIdToEntityId(entityClass, primaryKey);
        var entityKey = new EntityKey<>(entityClass, primaryKey, fieldIdType);

        if (hasAnyOneToOneEagerFetchType(entityMetadata)) {
            return getDao().findOneByWhereJoin(entityClass, primaryKey);
            //TODO: add to persistentContext
        }

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
        return Optional.empty();
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
        this.update(entityClass, entity, List.of());
    }

    @Override
    public int find(String query, Object[] bindValues) {
        flush();
        return bibernateSession.find(query, bindValues);
    }

    @Override
    public <T> T save(Class<T> entityClass, Object entity) {
        var insertEntityAction = new InsertEntityAction(bibernateSession, entityClass, entity);
        defaultActionQueue.addEntityAction(insertEntityAction);

        return entityClass.cast(entity);
    }

    @Override
    public <T> void deleteById(Class<T> entityClass, Object primaryKey) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(primaryKey, PRIMARY_KEY_MUST_BE_NOT_NULL);

        var fieldIdType = columnIdType(entityClass);
        primaryKey = castIdToEntityId(entityClass, primaryKey);
        var entityKey = new EntityKey<>(entityClass, primaryKey, fieldIdType);

        bibernateSession.deleteById(entityClass, primaryKey);

        removeCacheAndSnapshotBy(entityClass, primaryKey, entityKey);
    }

    @Override
    public <T> void delete(Class<T> entityClass, Object entity) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL);

        var deleteEntityAction = new DeleteEntityAction(bibernateSession, entityClass, entity);
        defaultActionQueue.addEntityAction(deleteEntityAction);

        var fieldIdType = columnIdType(entityClass);
        var primaryKey = columnIdValue(entityClass, entity);
        var entityKey = new EntityKey<>(entityClass, primaryKey, fieldIdType);

        removeCacheAndSnapshotBy(entityClass, primaryKey, entityKey);
    }

    @Override
    public void flush() {
        performDirtyChecking();
        defaultActionQueue.executeEntityAction();
    }

    @Override
    public void close() {
        log.trace(SESSION_IS_CLOSING_PERFORMING_DIRTY_CHECKING);
        performDirtyChecking();
        defaultActionQueue.executeEntityAction();
        BibernateSessionContextHolder.resetBibernateSession();

        clearCacheAndSnapshots();

        bibernateSession.close();
    }

    @Override
    public Dao getDao() {
        return bibernateSession.getDao();
    }

    private List<ColumnSnapshot> buildEntitySnapshot(Object entity) {
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL);

        Class<?> entityClass = entity.getClass();
        Field[] declaredFields = entityClass.getDeclaredFields();
        List<ColumnSnapshot> snapshot = new ArrayList<>(declaredFields.length);

        for (var field : declaredFields) {
            Object value = getValueFromObject(entity, field);
            String columnName = columnName(field);
            snapshot.add(new ColumnSnapshot(columnName, value, field.getType()));
        }

        return snapshot;
    }

    private <T> void update(Class<T> entityClass, Object entity, List<ColumnSnapshot> diff) {
        var updateEntityAction = new UpdateEntityAction(bibernateSession, entityClass, entity, diff);
        defaultActionQueue.addEntityAction(updateEntityAction);
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
        //TODO: add entire oneToOne entity to cache
            boolean isEntityHasLazyField = Arrays.stream(entityFromDb.getClass().getDeclaredFields())
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

    private <T> void removeCacheAndSnapshotBy(Class<T> entityClass, Object primaryKey, EntityKey<T> entityKey) {
        if (Objects.nonNull(firstLevelCache.remove(entityKey))) {
            log.trace(DELETED_ENTITY_CLASS_WITH_PRIMARY_KEY_FROM_FIRST_LEVEL_CACHE, entityClass, primaryKey);
        }
        if (Objects.nonNull(snapshots.remove(entityKey))) {
            log.trace(DELETED_ENTITY_CLASS_WITH_PRIMARY_KEY_FROM_SNAPSHOT, entityClass, primaryKey);
        }
    }

    private boolean hasAnyOneToOneEagerFetchType(EntityMetadata entityMetadata) {
        return entityMetadata.getEntityColumns().stream()
                .map(EntityColumnDetails::getOneToOne)
                .filter(Objects::nonNull)
                .anyMatch(oneToOneMetadata -> oneToOneMetadata.getFetchType() == FetchType.EAGER);
    }
}
