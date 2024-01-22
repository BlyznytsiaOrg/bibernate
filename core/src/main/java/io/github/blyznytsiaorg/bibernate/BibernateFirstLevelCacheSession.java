package io.github.blyznytsiaorg.bibernate;

import io.github.blyznytsiaorg.bibernate.dao.Dao;
import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;
import io.github.blyznytsiaorg.bibernate.entity.EntityKey;
import io.github.blyznytsiaorg.bibernate.utils.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class BibernateFirstLevelCacheSession implements BibernateSession {

    public static final String ENTITY_FOUND_IN_FIRST_LEVEL_CACHE_BY_ID = "Entity {} found in firstLevel cache by id {}";

    private final BibernateSession bibernateSession;
    private final Map<EntityKey<?>, Object> firstLevelCache = new HashMap<>();
    private final Map<EntityKey<?>, List<ColumnSnapshot>> snapshots = new HashMap<>();

    @Override
    public <T> Optional<T> findById(Class<T> entityClass, Object primaryKey) {
        var fieldIdType = columnIdType(entityClass);
        primaryKey  = castIdToEntityId(entityClass, primaryKey);
        var entityKey = new EntityKey<>(entityClass, primaryKey, fieldIdType);
        var cachedEntity = firstLevelCache.get(entityKey);

        if (Objects.isNull(cachedEntity)) {
            var finalPrimaryKey = primaryKey;
            
            return bibernateSession.findById(entityClass, primaryKey)
                    .map(entityFromDb -> {
                        firstLevelCache.put(entityKey, entityFromDb);
                        
                        List<ColumnSnapshot> entityCurrentSnapshot = buildEntitySnapshot(entityFromDb);
                        snapshots.put(entityKey, entityCurrentSnapshot);
                        
                        log.info("Create snapshot for entity {} id {}", entityClass.getSimpleName(), finalPrimaryKey);
                        log.info("Entity {} not found in firstLevel cache by id {}", entityClass.getSimpleName(), finalPrimaryKey);
                        
                        return entityFromDb;
                    });
        }

        log.info(ENTITY_FOUND_IN_FIRST_LEVEL_CACHE_BY_ID, entityClass.getSimpleName(), primaryKey);
        
        return Optional.of(entityClass.cast(cachedEntity));
    }

    private List<ColumnSnapshot> buildEntitySnapshot(Object entityClass) {
        Objects.requireNonNull(entityClass, "entityClass should not be null");
        Class<?> aClass = entityClass.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        List<ColumnSnapshot> snapshot = new ArrayList<>(declaredFields.length);

        for (var field : declaredFields) {
            Object value = getValueFromObject(entityClass, field);
            String columnName = columnName(field);
            snapshot.add(new ColumnSnapshot(columnName, value, field.getType()));
        }

        return snapshot;
    }

    private <T> void update(Class<T> entityClass, Object entity, List<ColumnSnapshot> diff) {
        var fieldIdType = columnIdType(entityClass);
        var fieldIdValue = columnIdValue(entityClass, entity);

        var entityKey = new EntityKey<>(entityClass, fieldIdValue, fieldIdType);
        T insertEntityFromDb = getDao().update(entityClass, entity, diff);
        firstLevelCache.put(entityKey, insertEntityFromDb);
        log.info("Update Entity {} in firstLevel cache by id {}", entityClass.getSimpleName(), fieldIdValue);
        
        List<ColumnSnapshot> entityCurrentSnapshot = buildEntitySnapshot(insertEntityFromDb);
        snapshots.put(entityKey, entityCurrentSnapshot);
        log.info("Update snapshot for entity {} id {}", entityClass.getSimpleName(), fieldIdValue);
    }

    private void performDirtyChecking() {
        firstLevelCache.keySet().forEach(entityKey -> {
            var entityInFirstLevelCache = firstLevelCache.get(entityKey);
            var entityInFirstLevelCacheCurrentSnapshot = buildEntitySnapshot(entityInFirstLevelCache);
            var entityOldSnapshot = snapshots.get(entityKey);
            var diff = getDifference(entityInFirstLevelCacheCurrentSnapshot, entityOldSnapshot);
            
            if (CollectionUtils.isNotEmpty(diff)) {
                log.info("Dirty entity found need to generate update for entityKey {} and entity {}", 
                        entityKey, entityInFirstLevelCache);
                update(entityInFirstLevelCache.getClass(), entityInFirstLevelCache, diff);
            } else {
                log.info("Dirty entity not found for entityKey {} no changes", entityKey);
            }
        });
    }
    @Override
    public void flush() {
        performDirtyChecking();
    }

    @Override
    public void close() {
        log.info("Session is closing. Performing dirty checking...");
        performDirtyChecking();
        log.info("FirstLevelCache is clearing...");
        firstLevelCache.clear();
    }

    @Override
    public Dao getDao() {
        return bibernateSession.getDao();
    }
}
