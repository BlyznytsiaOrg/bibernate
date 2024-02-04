package io.github.blyznytsiaorg.bibernate.session;

import io.github.blyznytsiaorg.bibernate.dao.Dao;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.join.JoinType;
import io.github.blyznytsiaorg.bibernate.entity.*;
import io.github.blyznytsiaorg.bibernate.utils.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.*;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.*;

/**
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class BibernateFirstLevelCacheSession implements BibernateSession {

    private final BibernateSession bibernateSession;
    private final Map<EntityKey<?>, Object> firstLevelCache = new HashMap<>();
    private final Map<EntityKey<?>, List<ColumnSnapshot>> snapshots = new HashMap<>();
    private final Map<Class<?>, EntityMetadata> bibernateEntityMetadata = BibernateEntityMetadataHolder.getBibernateEntityMetadata();

    @Override
    public <T> Optional<T> findById(Class<T> entityClass, Object primaryKey) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(primaryKey, PRIMARY_KEY_MUST_BE_NOT_NULL);

        EntityMetadata entityMetadata = bibernateEntityMetadata.get(entityClass);
        if (entityMetadata.getEntityColumns().stream()
                .anyMatch(EntityColumnDetails::isOneToOne)) {
//            tableName, whereCondition, joinedTable, onCondition(mainId, joinColumnNameId),
            getDao().findByWhereJoin(entityMetadata,   primaryKey);
        }
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
    public <T> List<T> findAllById(Class<T> entityClass, String idColumnName, Object idColumnValue) {
        var entities = bibernateSession.findAllById(entityClass, idColumnName, idColumnValue);
        persistentContext(entityClass, entities);

        return entities;
    }

    @Override
    public <T> List<T> findByWhere(Class<T> entityClass, String whereQuery, Object[] bindValues) {
        var entities = bibernateSession.findByWhere(entityClass, whereQuery, bindValues);
        persistentContext(entityClass, entities);

        return entities;
    }

    @Override
    public List<Object> findByWhereJoin(EntityMetadata searchedEntityMetadata, Object[] bindValues) {
        return null;
    }

    @Override
    public <T> List<T> findByQuery(Class<T> entityClass, String query, Object[] bindValues) {
        var entities = bibernateSession.findByQuery(entityClass, query, bindValues);
        persistentContext(entityClass, entities);

        return entities;
    }

    @Override
    public <T> int update(Class<T> entityClass, Object entity) {
        return this.update(entityClass, entity, List.of());
    }

    @Override
    public int find(String query, Object[] bindValues) {
        return bibernateSession.find(query, bindValues);
    }

    @Override
    public <T> T save(Class<T> entityClass, Object entity) {
        T entityFromDb = bibernateSession.save(entityClass, entity);
        persistentContext(entityClass, Collections.singletonList(entityFromDb));

        return entityFromDb;
    }

    @Override
    public <T> void deleteById(Class<T> entityClass, Object primaryKey) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(primaryKey, PRIMARY_KEY_MUST_BE_NOT_NULL);

        var fieldIdType = columnIdType(entityClass);
        primaryKey = castIdToEntityId(entityClass, primaryKey);
        var entityKey = new EntityKey<>(entityClass, primaryKey, fieldIdType);

        bibernateSession.deleteById(entityClass, primaryKey);

        if (Objects.nonNull(firstLevelCache.remove(entityKey))) {
            log.trace(DELETED_ENTITY_CLASS_WITH_PRIMARY_KEY_FROM_FIRST_LEVEL_CACHE, entityClass, primaryKey);
        }
        if (Objects.nonNull(snapshots.remove(entityKey))) {
            log.trace(DELETED_ENTITY_CLASS_WITH_PRIMARY_KEY_FROM_SNAPSHOT, entityClass, primaryKey);
        }
    }

    @Override
    public <T> void delete(Class<T> entityClass, Object entity) {
        Objects.requireNonNull(entityClass, ENTITY_CLASS_MUST_BE_NOT_NULL);
        Objects.requireNonNull(entity, ENTITY_MUST_BE_NOT_NULL);

        bibernateSession.delete(entityClass, entity);
    }

    @Override
    public void flush() {
        performDirtyChecking();
    }

    @Override
    public void close() {
        log.trace(SESSION_IS_CLOSING_PERFORMING_DIRTY_CHECKING);
        performDirtyChecking();
        BibernateSessionContextHolder.resetBibernateSession();

        log.trace(FIRST_LEVEL_CACHE_IS_CLEARING);
        firstLevelCache.clear();

        log.trace(SNAPSHOTS_ARE_CLEARING);
        snapshots.clear();

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

    private <T> int update(Class<T> entityClass, Object entity, List<ColumnSnapshot> diff) {
        var fieldIdType = columnIdType(entityClass);
        var fieldIdValue = columnIdValue(entityClass, entity);

        var entityKey = new EntityKey<>(entityClass, fieldIdValue, fieldIdType);
        var resultOfUpdate = getDao().update(entityClass, entity, diff);

        if (resultOfUpdate > 0) {
            firstLevelCache.put(entityKey, entity);
            log.trace(UPDATE_ENTITY_IN_FIRST_LEVEL_CACHE_BY_ID, entityClass.getSimpleName(), fieldIdValue);

            List<ColumnSnapshot> entityCurrentSnapshot = buildEntitySnapshot(entity);
            snapshots.put(entityKey, entityCurrentSnapshot);
            log.trace(UPDATE_SNAPSHOT_FOR_ENTITY_ID, entityClass.getSimpleName(), fieldIdValue);
        }

        return resultOfUpdate;
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
            var entityCashed = firstLevelCache.get(entityKey);

            if (Objects.nonNull(entityCashed)) {
                entities.set(i, entityClass.cast(entityCashed));
            } else {
                persistentContext(entityClass, entityFromDb, entityKey, fieldIdValue);
            }
        }
    }

    private <T> T persistentContext(Class<?> entityClass, T entityFromDb, EntityKey<?> entityKey,
                                    Object finalPrimaryKey) {
        if (!isImmutable(entityClass)) {
            firstLevelCache.put(entityKey, entityFromDb);

            List<ColumnSnapshot> entityCurrentSnapshot = buildEntitySnapshot(entityFromDb);
            snapshots.put(entityKey, entityCurrentSnapshot);

            log.trace(CREATED_SNAPSHOT_FOR_ENTITY_ID, entityClass.getSimpleName(), finalPrimaryKey);
        }

        return entityFromDb;
    }
}
