package io.github.blyznytsiaorg.bibernate.entity.metadata;

import static io.github.blyznytsiaorg.bibernate.utils.DDLUtils.getForeignKeyConstraintName;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;
import static io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils.getCascadeTypesFromAnnotation;

import io.github.blyznytsiaorg.bibernate.annotation.Column;
import io.github.blyznytsiaorg.bibernate.annotation.CreationTimestamp;
import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.ForeignKey;
import io.github.blyznytsiaorg.bibernate.annotation.GeneratedValue;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.JoinColumn;
import io.github.blyznytsiaorg.bibernate.annotation.JoinTable;
import io.github.blyznytsiaorg.bibernate.annotation.ManyToMany;
import io.github.blyznytsiaorg.bibernate.annotation.ManyToOne;
import io.github.blyznytsiaorg.bibernate.annotation.OneToMany;
import io.github.blyznytsiaorg.bibernate.annotation.OneToOne;
import io.github.blyznytsiaorg.bibernate.annotation.SequenceGenerator;
import io.github.blyznytsiaorg.bibernate.annotation.UpdateTimestamp;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.ColumnMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.CreationTimestampMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.GeneratedValueMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.IdMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.JoinColumnMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.JoinTableMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.ManyToManyMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.ManyToOneMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.OneToManyMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.OneToOneMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.SequenceGeneratorMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.UpdateTimestampMetadata;
import io.github.blyznytsiaorg.bibernate.exception.EntitiesNotFoundException;
import io.github.blyznytsiaorg.bibernate.exception.MappingException;
import io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Getter
@Slf4j
public class EntityMetadataCollector {
    public static final String ERROR_MESSAGE_ON_DUPLICATE_TABLE_NAME = "Detected duplicates for table name '%s' in classes '%s', '%s'";
    public static final String CANNOT_FIND_ANY_ENTITIES_ON_CLASSPATH_WITH_THIS_PACKAGE = "Cannot find any entities on classpath with this package %s";
    private final Reflections reflections;
    private final Map<Class<?>, EntityMetadata> inMemoryEntityMetadata;
    private final HashMap<String, Class<?>> tableNames;
    private final String packageName;

    public EntityMetadataCollector(String packageName) {
        this.packageName = packageName;
        this.reflections = new Reflections(packageName);
        this.inMemoryEntityMetadata = new HashMap<>();
        this.tableNames = new HashMap<>();
    }

    public Map<Class<?>, EntityMetadata> collectMetadata() {
        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);

        log.trace("Found entities size {}", entities.size());

        if (entities.isEmpty()) {
            throw new EntitiesNotFoundException(CANNOT_FIND_ANY_ENTITIES_ON_CLASSPATH_WITH_THIS_PACKAGE.formatted(packageName));
        }

        for (Class<?> entityClass : entities) {

            if (!inMemoryEntityMetadata.containsKey(entityClass)) {
                var tableName = table(entityClass);
                checkTableNameOnDuplicate(entityClass, tableName);

                boolean immutable = isImmutable(entityClass);
                boolean dynamicUpdate = isDynamicUpdate(entityClass);

                var entityMetadata = new EntityMetadata(tableName, immutable, dynamicUpdate, entityClass);

                for (Field field : entityClass.getDeclaredFields()) {
                    entityMetadata.addEntityColumn(createEntityColumnDetails(field, entityClass));
                }
                var indexMetadata = getIndexMetadata(entityClass);
                entityMetadata.addIndexMetadata(indexMetadata);

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

    private EntityColumnDetails createEntityColumnDetails(Field field, Class<?> entityClass) {
        var entityColumnDetails = EntityColumnDetails.builder()
                .field(field)
                .fieldName(field.getName())
                .fieldType(field.getType())
                .fieldType(isSupportedCollection(field) ? getCollectionGenericType(field) : field.getType())
                .isCollection(isSupportedCollection(field))
                .column(getColumn(field))
                .id(getId(field))
                .generatedValue(getGeneratedValue(field))
                .sequenceGenerator(getSequenceGenerator(field))
                .oneToOne(getOneToOne(field, entityClass))
                .oneToMany(getOneToMany(field))
                .manyToOne(getManyToOne(field))
                .manyToMany(getManyToMany(field, entityClass))
                .joinColumn(getJoinColumn(field))
                .joinTable(getJoinTable(field, entityClass))
                .creationTimestampMetadata(getCreationTimestampMetadata(field, entityClass))
                .updateTimestampMetadata(getUpdateTimestampMetadata(field, entityClass));

        return entityColumnDetails.build();
    }

    private UpdateTimestampMetadata getUpdateTimestampMetadata(Field field, Class<?> entityClass) {
        checkOnSufficientField(field, entityClass);
        if (field.isAnnotationPresent(UpdateTimestamp.class)) {
            return new UpdateTimestampMetadata();
        }
        return null;
    }


    private CreationTimestampMetadata getCreationTimestampMetadata(Field field,
                                                                   Class<?> entityClass) {
        checkOnSufficientField(field, entityClass);
        if (field.isAnnotationPresent(CreationTimestamp.class)) {
            return new CreationTimestampMetadata();
        }
        return null;
    }

    private void checkOnSufficientField(Field field, Class<?> entityClass) {
        if (field.isAnnotationPresent(CreationTimestamp.class) && field.isAnnotationPresent(UpdateTimestamp.class)) {
            throw new MappingException(("In class '%s' on field '%s' can't be @CreationTimestamp "
                    + "and @UpdateTimestamp annotations simultaneously")
                    .formatted(entityClass.getSimpleName(), field.getName()));
        }

        if (field.isAnnotationPresent(CreationTimestamp.class) || field.isAnnotationPresent(UpdateTimestamp.class)) {
            if (!isJavaTypeSufficientForTimestamps(field)) {
                throw new MappingException(("In class '%s' field '%s' with type '%s' is not supported "
                        + "for @CreationTimestamp or @UpdateTimestamp annotations")
                        .formatted(entityClass.getSimpleName(), field.getName(), field.getType().getSimpleName()));
            }
        }
    }

    private boolean isJavaTypeSufficientForTimestamps(Field field) {
        Class<?> fieldType = field.getType();
        return fieldType.equals(LocalDate.class)
                || fieldType.equals(OffsetTime.class) || fieldType.equals(LocalTime.class)
                || fieldType.equals(OffsetDateTime.class) || fieldType.equals(LocalDateTime.class);
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

    private JoinTableMetadata getJoinTable(Field field, Class<?> entityClass) {
        if (field.isAnnotationPresent(JoinTable.class) && (!field.isAnnotationPresent(ManyToMany.class))) {
            throw new MappingException((("No @ManyToMany annotation in class '%s' on field '%s' "
                    + "annotated with annotated @JoinTable")
                    .formatted(entityClass.getSimpleName(), field.getName())));
        }
        if (field.isAnnotationPresent(ManyToMany.class)) {

            return JoinTableMetadata
                    .builder()
                    .name(joinTableNameCorrect(field, entityClass))
                    .joinColumn(tableJoinColumnNameCorrect(field, entityClass))
                    .inverseJoinColumn(inverseTableJoinColumnName(field))
                    .joinColumnDatabaseType(joinColumnJoinTableDatabaseType(field, entityClass))
                    .inverseJoinColumnDatabaseType(inverseJoinColumnJoinTableDatabaseType(field))
                    .foreignKey(foreignKeyForJoinColumn(field))
                    .inverseForeignKey(foreignKeyForInverseJoinColumn(field))
                    .build();
        }
        return null;
    }

    private JoinColumnMetadata getJoinColumn(Field field) {

        if (field.isAnnotationPresent(JoinColumn.class)) {
            if (field.isAnnotationPresent(OneToMany.class)) {
                log.warn(("It is performance-efficient to map the relationship from the child side "
                        + "[field: '%s', @OneToMany]").formatted(field.getName()));
            } else if (!field.isAnnotationPresent(OneToOne.class) && !field.isAnnotationPresent(ManyToOne.class)) {
                throw new MappingException(("No @OneToOne or @ManyToOne annotation on field '%s' "
                        + "annotated with @JoinColumn").formatted(field.getName()));
            }
        }

        if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class)) {

            String joinColumnName = joinColumnName(field);
            String databaseTypeForJoinColumn = databaseTypeForJoinColumn(field);
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);

            Optional<String> key = Optional.ofNullable(field.getAnnotation(JoinColumn.class))
                    .map(annotation -> annotation.foreignKey())
                    .map(ForeignKey::name);
            String foreignKeyName;
            if (key.isEmpty() || key.get().isEmpty()) {
                foreignKeyName = getForeignKeyConstraintName();
            } else {
                foreignKeyName = key.get();
            }

            return JoinColumnMetadata.builder()
                    .name(joinColumnName)
                    .databaseType(databaseTypeForJoinColumn)
                    .foreignKeyName(foreignKeyName)
                    .build();
        }
        return null;
    }

    private ManyToManyMetadata getManyToMany(Field field, Class<?> entityClass) {
        if (isAnnotationPresent(field, ManyToMany.class)) {
            ManyToMany annotation = field.getAnnotation(ManyToMany.class);
            return ManyToManyMetadata
                    .builder()
                    .mappedBy(getMappedByForManyToMany(field, entityClass, annotation))
                    .cascadeTypes(getCascadeTypesFromAnnotation(annotation))
                    .build();
        }
        return null;
    }

    private String getMappedByForManyToMany(Field field, Class<?> entityClass,
                                            ManyToMany manyToMany) {
        if (!manyToMany.mappedBy().isEmpty()) {
            String entityClassSimpleName = entityClass.getSimpleName();
            Class<?> collectionGenericType = getCollectionGenericType(field);
            if (collectionGenericType.isAnnotationPresent(Entity.class)) {
                Field[] declaredFields = collectionGenericType.getDeclaredFields();
                boolean ifFieldHasParentEntity = Arrays.stream(declaredFields)
                        .filter(f -> f.isAnnotationPresent(ManyToMany.class))
                        .map(EntityReflectionUtils::getCollectionGenericType)
                        .anyMatch(f -> f.equals(entityClass));
                if (ifFieldHasParentEntity) {
                    return manyToMany.mappedBy();
                }
                throw new MappingException(("Can't find in entity '%s' @ManyToMany annotation "
                        + "as entity '%s' is annotated with @ManyToMany mappedBy='%s'")
                        .formatted(collectionGenericType.getSimpleName(), entityClassSimpleName,
                                manyToMany.mappedBy()));
            }
        }
        return manyToMany.mappedBy();
    }

    private ManyToOneMetadata getManyToOne(Field field) {
        if (isAnnotationPresent(field, ManyToOne.class)) {
            return ManyToOneMetadata.builder()
                    .cascadeTypes(getCascadeTypesFromAnnotation(field.getAnnotation(ManyToOne.class)))
                    .build();
        }
        return null;
    }

    private OneToManyMetadata getOneToMany(Field field) {
        if (isAnnotationPresent(field, OneToMany.class)) {
            OneToMany oneToManyAnnotation = field.getAnnotation(OneToMany.class);
            return OneToManyMetadata.builder()
                    .mappedByJoinColumnName(oneToManyAnnotation.mappedBy())
                    .cascadeTypes(getCascadeTypesFromAnnotation(oneToManyAnnotation))
                    .build();
        }
        return null;
    }

    private OneToOneMetadata getOneToOne(Field field, Class<?> entityClass) {
        if (isAnnotationPresent(field, OneToOne.class)) {
            OneToOne oneToOne = field.getAnnotation(OneToOne.class);
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
                    .fetchType(oneToOne.fetch())
                    .parentClass(parentClass)
                    .childClass(childClass)
                    .mappedBy(getMappedByForOneToOne(field, entityClass, oneToOne))
                    .cascadeTypes(getCascadeTypesFromAnnotation(field.getAnnotation(OneToOne.class)))
                    .build();
        }
        return null;
    }

    private String getMappedByForOneToOne(Field field, Class<?> entityClass, OneToOne oneToOne) {
        if (!oneToOne.mappedBy().isEmpty()) {
            String entityClassSimpleName = entityClass.getSimpleName();
            Class<?> fieldType = field.getType();
            if (fieldType.isAnnotationPresent(Entity.class)) {
                Field[] declaredFields = fieldType.getDeclaredFields();
                boolean ifFieldHasParentEntity = Arrays.stream(declaredFields)
                        .filter(f -> f.isAnnotationPresent(OneToOne.class))
                        .anyMatch(f -> f.getType().equals(entityClass));
                if (ifFieldHasParentEntity) {
                    return oneToOne.mappedBy();
                }
                throw new MappingException(("Can't find in entity '%s' @OneToOne annotation "
                        + "as entity '%s' is annotated with @OneToOne mappedBy='%s'")
                        .formatted(fieldType.getSimpleName(), entityClassSimpleName,
                                oneToOne.mappedBy()));
            }
        }
        return oneToOne.mappedBy();
    }

    private IdMetadata getId(Field field) {
        if (isAnnotationPresent(field, Id.class)) {
            return new IdMetadata();
        }
        return null;
    }

    private ColumnMetadata getColumn(Field field) {
        String columnName = columnName(field);
        String databaseType = databaseTypeForInternalJavaType(field);
        boolean isTimeZone = isTimeZone(field);
        boolean isTimestamp = isTimestamp(field);
        ColumnMetadata.ColumnMetadataBuilder builder = ColumnMetadata.builder()
                .name(columnName)
                .databaseType(databaseType)
                .timeZone(isTimeZone)
                .timestamp(isTimestamp)
                .unique(false)
                .nullable(true)
                .columnDefinition("");

        Column annotation = field.getAnnotation(Column.class);
        if (annotation != null) {
            builder.unique(annotation.unique())
                    .nullable(annotation.nullable())
                    .columnDefinition(annotation.columnDefinition());
        }
        return builder.build();
    }
}
