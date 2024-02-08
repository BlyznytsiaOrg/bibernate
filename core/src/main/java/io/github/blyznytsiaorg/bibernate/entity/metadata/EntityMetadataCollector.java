package io.github.blyznytsiaorg.bibernate.entity.metadata;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.columnName;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.inverseTableJoinColumnName;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.isAnnotationPresent;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.isDynamicUpdate;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.isImmutable;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.joinColumnName;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.joinTableName;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.mappedByJoinColumnName;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.table;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.tableJoinColumnName;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.ColumnMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.GeneratedValueMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.IdMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.JoinColumnMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.JoinTableMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.ManyToManyMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.ManyToOneMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.OneToManyMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.OneToOneMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.SequenceGeneratorMetadata;
import io.github.blyznytsiaorg.bibernate.exception.MappingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
@Slf4j
public class EntityMetadataCollector {
    public static final String ERROR_MESSAGE_ON_DUPLICATE_TABLE_NAME = "Detected duplicates for table name '%s' in classes '%s', '%s'";
    private final Reflections reflections;
    private final Map<Class<?>, EntityMetadata> inMemoryEntityMetadata;
    private final HashMap<String, Class<?>> tableNames;

    public EntityMetadataCollector(String baseEntityPackage) {
        this.reflections = new Reflections(baseEntityPackage);
        this.inMemoryEntityMetadata = new HashMap<>();
        this.tableNames = new HashMap<>();
    }

    public Map<Class<?>, EntityMetadata> collectMetadata() {
        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);

        for (Class<?> entityClass : entities) {

            if (!inMemoryEntityMetadata.containsKey(entityClass)) {
                var tableName = table(entityClass);
                checkTableNameOnDuplicate(entityClass, tableName);

                boolean immutable = isImmutable(entityClass);
                boolean dynamicUpdate = isDynamicUpdate(entityClass);

                var entityMetadata = new EntityMetadata(tableName, immutable, dynamicUpdate, entityClass);

                for (Field field : entityClass.getDeclaredFields()) {
                    entityMetadata.addEntityColumn(createEntityColumnDetails(field));
                }

                inMemoryEntityMetadata.put(entityClass, entityMetadata);
            }
        }
        return inMemoryEntityMetadata;
    }

    private void checkTableNameOnDuplicate(Class<?> entityClass, String tableName) {
        if (tableNames.containsKey(tableName)) {
            Class<?> classWithSameTableName = tableNames.get(tableName);
            throw new MappingException(ERROR_MESSAGE_ON_DUPLICATE_TABLE_NAME
                    .formatted(tableName, entityClass.getSimpleName(), classWithSameTableName.getSimpleName()));
        } else {
            tableNames.put(tableName, entityClass);
        }
    }

    private EntityColumnDetails createEntityColumnDetails(Field field) {
        var entityColumnDetails = EntityColumnDetails.builder()
                .field(field)
                .fieldName(field.getName())
                .fieldType(field.getType())
                .column(getColumn(field))
                .id(getId(field))
                .generatedValue(getGeneratedValue(field))
                .sequenceGenerator(getSequenceGenerator(field))
                .oneToOne(getOneToOne(field))
                .oneToMany(getOneToMany(field))
                .manyToOne(getManyToOne(field))
                .manyToMany(getManyToMany(field))
                .joinColumn(getJoinColumn(field))
                .joinTable(getJoinTable(field));

        return entityColumnDetails.build();
    }

    private SequenceGeneratorMetadata getSequenceGenerator(Field field) {
        if (isAnnotationPresent(field, SequenceGenerator.class)) {
            SequenceGenerator annotation = field.getAnnotation(SequenceGenerator.class);
            String generatorName = annotation.name();
            String sequenceName = annotation.sequenceName();
            int initialValue = annotation.initialValue();
            int allocationSize = annotation.allocationSize();
            return new SequenceGeneratorMetadata
                    (generatorName, sequenceName, initialValue, allocationSize);
        }
        return null;
    }

    private GeneratedValueMetadata getGeneratedValue(Field field) {
        if (isAnnotationPresent(field, GeneratedValue.class)) {
            GeneratedValue annotation = field.getAnnotation(GeneratedValue.class);
            String generatedStrategyName = annotation.strategy().name();
            String generatorName = annotation.generator();
            return new GeneratedValueMetadata(generatedStrategyName, generatorName);
        }
        return null;
    }

    private JoinTableMetadata getJoinTable(Field field) {
        if (isAnnotationPresent(field, JoinTable.class)) {
            return JoinTableMetadata
                    .builder()
                    .name(joinTableName(field))
                    .joinColumn(tableJoinColumnName(field))
                    .inverseJoinColumn(inverseTableJoinColumnName(field))
                    .build();
        }
        return null;
    }

    private JoinColumnMetadata getJoinColumn(Field field) {
        if (isAnnotationPresent(field, JoinColumn.class)) {
            String joinColumnName = joinColumnName(field);
            return JoinColumnMetadata
                    .builder()
                    .name(joinColumnName)
                    .build();
        }
        return null;
    }

    private ManyToManyMetadata getManyToMany(Field field) {
        if (isAnnotationPresent(field, ManyToMany.class)) {
            ManyToMany annotation = field.getAnnotation(ManyToMany.class);
            String mappedBy = annotation.mappedBy();
            return ManyToManyMetadata
                    .builder()
                    .mappedBy(mappedBy)
                    .build();
        }
        return null;
    }

    private ManyToOneMetadata getManyToOne(Field field) {
        if (isAnnotationPresent(field, ManyToOne.class)) {
            return new ManyToOneMetadata();
        }
        return null;
    }

    private OneToManyMetadata getOneToMany(Field field) {
        if (isAnnotationPresent(field, OneToMany.class)) {
            String mappedByJoinColumnName = mappedByJoinColumnName(field);
            return OneToManyMetadata.builder()
                    .mappedByJoinColumnName(mappedByJoinColumnName)
                    .build();
        }
        return null;
    }

    private OneToOneMetadata getOneToOne(Field field) {
        if (isAnnotationPresent(field, OneToOne.class)) {
            FetchType fetchType = field.getAnnotation(OneToOne.class).fetch();
            Class<?> parentClass;
            Class<?> childClass;

            if (field.isAnnotationPresent(JoinColumn.class)) {
                parentClass = field.getType();
                childClass = field.getDeclaringClass();
            } else {
                parentClass = field.getDeclaringClass();
                childClass = field.getType();
            }
            return OneToOneMetadata.builder()
                    .joinedTable(table(field.getType()))
                    .fetchType(fetchType)
                    .parentClass(parentClass)
                    .childClass(childClass)
                    .build();
        }

        return null;
    }

    private IdMetadata getId(Field field) {
        if (isAnnotationPresent(field, Id.class)) {
            return new IdMetadata();
        }
        return null;
    }

    private ColumnMetadata getColumn(Field field) {
        String columnName = columnName(field);
        return new ColumnMetadata(columnName);
    }
}
