package io.github.blyznytsiaorg.bibernate.entity;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

@Getter
@Slf4j
public class EntityMetadataCollector {
    private final Reflections reflections;
    private final Map<Class<?>, EntityMetadata> inMemoryEntityMetadata;

    public EntityMetadataCollector(String baseEntityPackage) {
        this.reflections = new Reflections(baseEntityPackage);
        this.inMemoryEntityMetadata = new HashMap<>();
    }

    public void startCollectMetadata() {
        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);

        for (Class<?> entityClass : entities) {

            if (!inMemoryEntityMetadata.containsKey(entityClass)) {
                var tableName = table(entityClass);
                boolean immutable = isImmutable(entityClass);
                boolean dynamicUpdate = isDynamicUpdate(entityClass);
                var idColumnName = columnIdName(entityClass);

                var entityMetadata = new EntityMetadata(tableName, idColumnName, immutable, dynamicUpdate);

                for (Field field : entityClass.getDeclaredFields()) {
                    entityMetadata.addEntityColumn(createEntityColumnDetails(field));
                }

                inMemoryEntityMetadata.put(entityClass, entityMetadata);
            }
        }
        resolveRelations(entities);
    }

    private void resolveRelations(Set<Class<?>> entities) {
        for (Class<?> entityClass : entities) {
            if (inMemoryEntityMetadata.containsKey(entityClass)) {
                EntityMetadata entityMetadata = inMemoryEntityMetadata.get(entityClass);
                for (EntityColumnDetails entityColumn : entityMetadata.getEntityColumns()) {
                    resolveOneToOne(entityColumn, entityMetadata);
                }
            }
        }
    }

    private void resolveOneToOne(EntityColumnDetails entityColumn, EntityMetadata entityMetadata) {
        if (entityColumn.isOneToOne()) {
            entityMetadata.setHasOneToOne(true);
            if (entityColumn.isJoinColumn()) {
                var parentEntity = inMemoryEntityMetadata.get(entityColumn.getFieldType());
                entityMetadata.setOneToOneInfo(new OneToOneInfo(entityMetadata, parentEntity));
            } else {
                var childEntity = inMemoryEntityMetadata.get(entityColumn.getFieldType());
                entityMetadata.setOneToOneInfo(new OneToOneInfo(childEntity, entityMetadata));
            }
        }
    }

    private EntityColumnDetails createEntityColumnDetails(Field field) {
        var entityColumnDetails = EntityColumnDetails.builder()
                .fieldName(field.getName())
                .columnId(isColumnHasAnnotation(field, Id.class))
                .fieldType(field.getType())
                .fieldColumnName(columnName(field));

        boolean isFieldHasAnnotationOneToOne = isColumnHasAnnotation(field, OneToOne.class);
        boolean isFieldHasAnnotationManyToMany = isColumnHasAnnotation(field, ManyToOne.class);

        entityColumnDetails.oneToOne(isFieldHasAnnotationOneToOne)
                .manyToOne(isFieldHasAnnotationManyToMany);

        if (isFieldHasAnnotationOneToOne || isFieldHasAnnotationManyToMany) {
            entityColumnDetails.fetchType(field.getAnnotation(OneToOne.class).fetch());
            boolean isFieldHasAnnotationJoinColumn = isColumnHasAnnotation(field, JoinColumn.class);
            entityColumnDetails.joinColumnTableName(table(field.getType()));

            if (isFieldHasAnnotationJoinColumn) {
                entityColumnDetails.joinColumn(true)
                        .joinColumnName(field.getAnnotation(JoinColumn.class).name());
            }
        }

        return entityColumnDetails.build();
    }
}
