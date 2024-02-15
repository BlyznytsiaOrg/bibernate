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

/**
 * The EntityMetadataCollector class is responsible for collecting metadata for entities within a specified package.
 * It utilizes reflection to scan for classes annotated with @Entity and gathers metadata such as table names,
 * column details and index metadata.
 *
 * @see EntityMetadata
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Getter
@Slf4j
public class EntityMetadataCollector {
    public static final String ERROR_MESSAGE_ON_DUPLICATE_TABLE_NAME = "Detected duplicates for table name '%s' in classes '%s', '%s'";
    public static final String CANNOT_FIND_ANY_ENTITIES_ON_CLASSPATH_WITH_THIS_PACKAGE = "Cannot find any entities on classpath with this package %s";
    private final Reflections reflections;
    private final Map<Class<?>, EntityMetadata> inMemoryEntityMetadata;
    private final HashMap<String, Class<?>> tableNames;
    private final String packageName;

    /**
     * Constructs an EntityMetadataCollector object with the specified package name.
     * It initializes the Reflections object to scan the package for entity classes and initializes
     * internal data structures to store metadata.
     *
     * @param packageName The name of the package to scan for entity classes.
     */
    public EntityMetadataCollector(String packageName) {
        this.packageName = packageName;
        this.reflections = new Reflections(packageName);
        this.inMemoryEntityMetadata = new HashMap<>();
        this.tableNames = new HashMap<>();
    }


    /**
     * Collects metadata for entities within the specified package.
     * It scans for classes annotated with @Entity using reflection, gathers metadata such as table names,
     * column details, index metadata and stores them in a map.
     *
     * @return A map containing entity classes as keys and their corresponding metadata as values.
     * @throws EntitiesNotFoundException if no entities are found in the specified package.
     */
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

    /**
     * Checks if the table name is already associated with another entity class.
     * If a duplicate table name is found, it throws a MappingException.
     *
     * @param entityClass The entity class for which the table name is being checked.
     * @param tableName   The table name to check for duplicates.
     * @throws MappingException if a duplicate table name is found.
     */
    private void checkTableNameOnDuplicate(Class<?> entityClass, String tableName) {
        if (tableNames.containsKey(tableName)) {
            Class<?> classWithSameTableName = tableNames.get(tableName);
            throw new MappingException(ERROR_MESSAGE_ON_DUPLICATE_TABLE_NAME
                    .formatted(tableName, entityClass.getSimpleName(), classWithSameTableName.getSimpleName()));
        } else {
            tableNames.put(tableName, entityClass);
        }
    }

    /**
     * Creates and initializes an EntityColumnDetails object based on the provided field and entity class.
     *
     * @param field       The field for which EntityColumnDetails is being created.
     * @param entityClass The entity class to which the field belongs.
     * @return An initialized EntityColumnDetails object.
     */
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

    /**
     * Retrieves metadata for an update timestamp field if the field is annotated with @UpdateTimestamp.
     *
     * @param field       The field to check for the @UpdateTimestamp annotation.
     * @param entityClass The class to which the field belongs.
     * @return UpdateTimestampMetadata object if the field has the @UpdateTimestamp annotation; otherwise, null.
     */
    private UpdateTimestampMetadata getUpdateTimestampMetadata(Field field, Class<?> entityClass) {
        checkOnSufficientField(field, entityClass);
        if (field.isAnnotationPresent(UpdateTimestamp.class)) {
            return new UpdateTimestampMetadata();
        }
        return null;
    }


    /**
     * Retrieves metadata for a creation timestamp field if the field is annotated with @CreationTimestamp.
     *
     * @param field       The field to check for the @CreationTimestamp annotation.
     * @param entityClass The class to which the field belongs.
     * @return CreationTimestampMetadata object if the field has the @CreationTimestamp annotation; otherwise, null.
     */
    private CreationTimestampMetadata getCreationTimestampMetadata(Field field,
                                                                   Class<?> entityClass) {
        checkOnSufficientField(field, entityClass);
        if (field.isAnnotationPresent(CreationTimestamp.class)) {
            return new CreationTimestampMetadata();
        }
        return null;
    }

    /**
     * Checks whether the field has both @CreationTimestamp and @UpdateTimestamp annotations simultaneously.
     * Additionally, it verifies if the field's type is suitable for @CreationTimestamp or @UpdateTimestamp annotations.
     *
     * @param field       The field to check for annotations and type suitability.
     * @param entityClass The class to which the field belongs.
     * @throws MappingException if the field has both @CreationTimestamp and @UpdateTimestamp annotations simultaneously,
     *                          or if the field's type is not suitable for @CreationTimestamp or @UpdateTimestamp annotations.
     */
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

    /**
     * Checks if the given field's type is sufficient for use with @CreationTimestamp or @UpdateTimestamp annotations.
     * Supported types include LocalDate, OffsetTime, LocalTime, OffsetDateTime, and LocalDateTime.
     *
     * @param field The field to check.
     * @return {@code true} if the field's type is one of the supported timestamp types, {@code false} otherwise.
     */
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

    /**
     * Retrieves the metadata for a sequence generator annotation applied to the given field.
     *
     * @param field The field to inspect for the presence of a SequenceGenerator annotation.
     * @return SequenceGeneratorMetadata if the field is annotated with SequenceGenerator, null otherwise.
     */
    private GeneratedValueMetadata getGeneratedValue(Field field) {
        if (isAnnotationPresent(field, GeneratedValue.class)) {
            GeneratedValue annotation = field.getAnnotation(GeneratedValue.class);
            String generatedStrategyName = annotation.strategy().name();
            String generatorName = annotation.generator();
            return new GeneratedValueMetadata(generatedStrategyName, generatorName);
        }
        return null;
    }

    /**
     * Retrieves the metadata for a JoinTable annotation applied to the given field.
     *
     * @param field       The field to inspect for the presence of a JoinTable annotation.
     * @param entityClass The class of the entity to which the field belongs.
     * @return JoinTableMetadata if the field is annotated with JoinTable and is a ManyToMany association, null otherwise.
     * @throws MappingException if the field is annotated with JoinTable but not annotated with ManyToMany.
     */
    private JoinTableMetadata getJoinTable(Field field, Class<?> entityClass) {
        if (field.isAnnotationPresent(JoinTable.class) && (!field.isAnnotationPresent(ManyToMany.class))) {
            throw new MappingException((("No @ManyToMany annotation is set in class '%s' on field '%s' "
                    + "annotated with @JoinTable annotation")
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

    /**
     * Retrieves the metadata for a JoinColumn annotation applied to the given field.
     *
     * @param field The field to inspect for the presence of a JoinColumn annotation.
     * @return JoinColumnMetadata if the field is annotated with JoinColumn and is a OneToOne or ManyToOne association, null otherwise.
     * @throws MappingException if the field is annotated with JoinColumn but not annotated with OneToOne or ManyToOne.
     */
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
            Optional<String> key = Optional.ofNullable(field.getAnnotation(JoinColumn.class))
                    .flatMap(annotation -> Optional.ofNullable(annotation.foreignKey()))
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

    /**
     * Retrieves the metadata for a ManyToMany association defined by the given field in the specified entity class.
     *
     * @param field The field representing the ManyToMany association.
     * @param entityClass The entity class containing the ManyToMany association.
     * @return ManyToManyMetadata if the field is annotated with ManyToMany, null otherwise.
     */
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

    /**
     * Retrieves the mappedBy attribute value for a ManyToMany association defined by the given field in the specified entity class.
     *
     * @param field The field representing the ManyToMany association.
     * @param entityClass The entity class containing the ManyToMany association.
     * @param manyToMany The ManyToMany annotation instance.
     * @return The mappedBy attribute value if not empty and valid, otherwise an empty string.
     * @throws MappingException If the mappedBy attribute is not found in the target entity.
     */
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

    /**
     * Retrieves metadata related to ManyToOne association for the specified field.
     *
     * @param field The field representing the ManyToOne association.
     * @return Metadata related to ManyToOne association if the field is annotated with ManyToOne, otherwise null.
     */
    private ManyToOneMetadata getManyToOne(Field field) {
        if (isAnnotationPresent(field, ManyToOne.class)) {
            return ManyToOneMetadata.builder()
                    .cascadeTypes(getCascadeTypesFromAnnotation(field.getAnnotation(ManyToOne.class)))
                    .build();
        }
        return null;
    }

    /**
     * Retrieves metadata related to OneToMany association for the specified field.
     *
     * @param field The field representing the OneToMany association.
     * @return Metadata related to OneToMany association if the field is annotated with OneToMany, otherwise null.
     */
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

    /**
     * Retrieves metadata related to OneToOne association for the specified field.
     *
     * @param field       The field representing the OneToOne association.
     * @param entityClass The class of the entity containing the field.
     * @return Metadata related to OneToOne association if the field is annotated with OneToOne, otherwise null.
     */
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

    /**
     * Retrieves metadata related to the identifier (ID) for the specified field.
     *
     * @param field The field for which ID metadata is retrieved.
     * @return Metadata related to the ID if the field is annotated with @Id, otherwise null.
     */
    private IdMetadata getId(Field field) {
        if (isAnnotationPresent(field, Id.class)) {
            return new IdMetadata();
        }
        return null;
    }

    /**
     * Retrieves metadata related to the column for the specified field.
     *
     * @param field The field for which column metadata is retrieved.
     * @return Metadata related to the column.
     */
    private ColumnMetadata getColumn(Field field) {
        String columnName = columnName(field);
        String databaseType = databaseTypeForInternalJavaType(field);
        ColumnMetadata.ColumnMetadataBuilder builder = ColumnMetadata.builder()
                .name(columnName)
                .databaseType(databaseType)
                .unique(false)
                .nullable(true)
                .columnDefinition("")
                .timestamp(isTimestamp(field))
                .timeZone(isTimeZone(field));

        Column annotation = field.getAnnotation(Column.class);
        if (annotation != null) {
            builder.unique(annotation.unique())
                    .nullable(annotation.nullable())
                    .columnDefinition(annotation.columnDefinition());
        }
        return builder.build();
    }
}
