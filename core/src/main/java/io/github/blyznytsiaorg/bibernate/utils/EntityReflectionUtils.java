package io.github.blyznytsiaorg.bibernate.utils;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.identity.SequenceConf;
import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;
import io.github.blyznytsiaorg.bibernate.entity.EntityColumn;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.IndexMetadata;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.exception.BibernateValidationException;
import io.github.blyznytsiaorg.bibernate.exception.MappingException;
import io.github.blyznytsiaorg.bibernate.exception.MissingAnnotationException;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static io.github.blyznytsiaorg.bibernate.annotation.GenerationType.IDENTITY;
import static io.github.blyznytsiaorg.bibernate.annotation.GenerationType.SEQUENCE;
import static io.github.blyznytsiaorg.bibernate.utils.DDLUtils.getForeignKeyConstraintName;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_FIND_SEQUENCE_STRATEGY;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.ENTITY_MUST_BE_NOT_NULL;
import static io.github.blyznytsiaorg.bibernate.utils.TypeConverter.convertToDatabaseType;


/**
 * Utility class providing reflection-based operations for entity classes.
 * This class offers various methods for introspecting entity classes, retrieving information about fields,
 * annotations, and metadata, and performing tasks such as getting table names, column names, and more.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
@UtilityClass
public class EntityReflectionUtils {
    private static final String UNABLE_TO_GET_ID_NAME_FOR_ENTITY = "Unable to get id name for entity [%s]";
    private static final String UNABLE_TO_GET_VERSION_NAME_FOR_ENTITY = "Unable to get version name for entity [%s]";
    public static final String UNABLE_TO_GET_ID_FIELD_FOR_ENTITY = "Unable to get id field for entity [%s]";
    public static final String UNABLE_TO_GET_GENERATED_VALUE_FIELD_FOR_ENTITY = "Unable to get generated value field for entity [%s]";

    private static final String SNAKE_REGEX = "([a-z])([A-Z]+)";
    private static final String REPLACEMENT = "$1_$2";
    private static final String ID_POSTFIX = "_id";
    public static final String JOIN_TABLE_NAME_PATTERN = "%s_%s";
    public static final String ENTITY_S_SHOULD_HAVE_ID_THAT_NOT_NULL_OR_ADD_ANNOTATION_GENERATED_VALUE = "Entity %s should have Id that not null or add annotation @GeneratedValue";

    /**
     * Retrieves the table name associated with the given entity class.
     * If the entity class has a {@code Table} annotation with a non-empty name,
     * that name is returned. Otherwise, the name is derived from the entity class name
     * by converting it from camel case to snake case.
     *
     * @param entityClass The class of the entity for which to retrieve the table name.
     * @return The table name associated with the entity class.
     */
    public static String table(Class<?> entityClass) {
        return Optional.ofNullable(entityClass.getAnnotation(Table.class))
                .map(Table::name)
                .filter(Predicate.not(String::isEmpty))
                .orElse(getSnakeString(entityClass.getSimpleName()));
    }

    /**
     * Checks whether the given entity class is marked as immutable.
     *
     * @param entityClass The class of the entity to check.
     * @return {@code true} if the entity class is marked as immutable, {@code false} otherwise.
     */
    public static boolean isImmutable(Class<?> entityClass) {
        return entityClass.isAnnotationPresent(Immutable.class);
    }

    /**
     * Checks whether the given entity class is annotated with DynamicUpdate.
     *
     * @param entityClass The class of the entity to check.
     * @return {@code true} if the entity class is annotated with DynamicUpdate, {@code false} otherwise.
     */
    public static boolean isDynamicUpdate(Class<?> entityClass) {
        return entityClass.isAnnotationPresent(DynamicUpdate.class);
    }

    /**
     * Checks whether the given field has the specified annotation.
     *
     * @param field           The field to check.
     * @param annotationClass The class object representing the annotation to look for.
     * @return {@code true} if the field has the specified annotation, {@code false} otherwise.
     */
    public static boolean isColumnHasAnnotation(Field field, Class<? extends Annotation> annotationClass) {
        return field.isAnnotationPresent(annotationClass);
    }


    /**
     * Retrieves the index metadata for the specified entity class.
     *
     * @param entityClass The class of the entity to retrieve index metadata for.
     * @return A list of index metadata for the entity, or an empty list if no index metadata is found.
     */
    public static List<IndexMetadata> getIndexMetadata(Class<?> entityClass) {
        return Optional.ofNullable(entityClass.getAnnotation(Table.class))
                .map(table -> Arrays.stream(table.indexes())
                        .map(index -> new IndexMetadata(index.name(), index.columnList()))
                        .toList())
                .orElseGet(ArrayList::new);
    }

    /**
     * Retrieves the column name for the specified field.
     *
     * @param field The field to retrieve the column name for.
     * @return The column name, or a generated name if not specified in annotations.
     */
    public static String columnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(Column.class))
                .map(Column::name)
                .filter(Predicate.not(String::isEmpty))
                .or(() -> getJoinColumnName(field))
                .orElse(getSnakeString(field.getName()));
    }

    /**
     * Retrieves the join column name for the specified field, if present.
     *
     * @param field The field to retrieve the join column name for.
     * @return An optional containing the join column name, or empty if not specified.
     */
    private static Optional<String> getJoinColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(JoinColumn.class))
                .map(JoinColumn::name)
                .filter(Predicate.not(String::isEmpty));
    }


    /**
     * Retrieves the column name for the specified field, considering the type of association if present.
     *
     * @param declaredField The field for which to retrieve the column name.
     * @param type          The type of the field.
     * @return The column name.
     */
    public static String columnName(Field declaredField, Class<?> type) {
        String columnName;

        if (declaredField.isAnnotationPresent(OneToOne.class) && declaredField.isAnnotationPresent(JoinColumn.class)) {
            columnName = declaredField.getAnnotation(JoinColumn.class).name();
        } else {
            columnName = columnName(declaredField);
        }
        return columnName;
    }

    /**
     * Retrieves the database type for the internal Java type of the specified field.
     *
     * @param field The field for which to retrieve the database type.
     * @return The database type.
     */
    public static String databaseTypeForInternalJavaType(Field field) {
        Column annotation = field.getAnnotation(Column.class);
        String columnDefinition = (annotation != null) ? annotation.columnDefinition() : "";
        return columnDefinition.isEmpty() ? convertToDatabaseType(field.getType()) : columnDefinition;
    }

    /**
     * Checks if the specified field represents a time zone.
     *
     * @param field The field to check.
     * @return True if the field represents a time zone, false otherwise.
     */
    public static boolean isTimeZone(Field field) {
        Class<?> fieldType = field.getType();
        return fieldType.equals(OffsetTime.class) || fieldType.equals(OffsetDateTime.class);
    }

    /**
     * Checks if the specified field represents a timestamp.
     *
     * @param field The field to check.
     * @return True if the field represents a timestamp, false otherwise.
     */
    public static boolean isTimestamp(Field field) {
        Class<?> fieldType = field.getType();
        return fieldType.equals(OffsetTime.class) || fieldType.equals(OffsetDateTime.class)
                || fieldType.equals(LocalDate.class) || fieldType.equals(LocalTime.class)
                || fieldType.equals(LocalDateTime.class);
    }


    /**
     * Retrieves the join table name specified by the @JoinTable annotation on the given field.
     *
     * @param field The field representing the association.
     * @return The name of the join table, or null if not specified.
     */
    public static String joinTableName(Field field) {
        return Optional.ofNullable(field.getAnnotation(JoinTable.class))
                .map(JoinTable::name)
                .filter(Predicate.not(String::isEmpty))
                .orElse(null);
    }

    /**
     * Retrieves the correct join table name for the specified field and entity class. If the field is annotated with @ManyToMany and has no 'mappedBy' attribute, it retrieves the join table name. Otherwise, it constructs a default name based on the entity names involved in the association.
     *
     * @param field       The field representing the association.
     * @param entityClass The entity class containing the association.
     * @return The join table name if specified, or the default name if applicable, otherwise null.
     */
    public static String joinTableNameCorrect(Field field, Class<?> entityClass) {
        if (field.isAnnotationPresent(ManyToMany.class)) {
            ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
            if (manyToMany.mappedBy().isEmpty()) {
                return Optional.ofNullable(field.getAnnotation(JoinTable.class))
                        .map(JoinTable::name)
                        .filter(Predicate.not(String::isEmpty))
                        .orElseGet(() -> getManyManyDefaultTableName(field, entityClass));
            }
        }
        return null;
    }

    /**
     * Constructs a default join table name for a @ManyToMany association based on the names of the entities involved.
     *
     * @param field       The field representing the association.
     * @param entityClass The entity class containing the association.
     * @return The default join table name.
     */
    private static String getManyManyDefaultTableName(Field field, Class<?> entityClass) {
        if (field.isAnnotationPresent(ManyToMany.class)) {
            Class<?> collectionGenericType = EntityReflectionUtils.getCollectionGenericType(field);
            String thisEntityTableName = table(entityClass);
            String relationEntityTableName = table(collectionGenericType);
            return JOIN_TABLE_NAME_PATTERN.formatted(thisEntityTableName, relationEntityTableName);
        }
        return null;
    }


    /**
     * Retrieves the correct join column name for the specified field and entity class. If the field is annotated with @ManyToMany and has no 'mappedBy' attribute, it retrieves the join column name specified in the @JoinTable annotation. Otherwise, it constructs a default name based on the entity name and its primary key column.
     *
     * @param field       The field representing the association.
     * @param entityClass The entity class containing the association.
     * @return The correct join column name if specified, otherwise null.
     */
    public static String tableJoinColumnNameCorrect(Field field, Class<?> entityClass) {
        if (field.isAnnotationPresent(ManyToMany.class)) {
            ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
            if (manyToMany.mappedBy().isEmpty()) {
                return Optional.ofNullable(field.getAnnotation(JoinTable.class))
                        .map(JoinTable::joinColumn)
                        .map(JoinColumn::name)
                        .orElseGet(() -> defaultTableJoinColumnName(entityClass));
            }
        }
        return null;
    }

    /**
     * Constructs a default join column name for a @ManyToMany association based on the entity class name and its primary key column name.
     *
     * @param entityClass The entity class containing the association.
     * @return The default join column name.
     */
    private static String defaultTableJoinColumnName(Class<?> entityClass) {
        String columnIdName = columnIdName(entityClass);
        return JOIN_TABLE_NAME_PATTERN.formatted(entityClass.getSimpleName().toLowerCase(), columnIdName);
    }

    /**
     * Retrieves the inverse join column name for the specified field, if applicable. This method is used in the context of Many-to-Many associations where 'mappedBy' is not specified.
     *
     * @param field The field representing the association.
     * @return The inverse join column name if specified, otherwise null.
     */
    public static String inverseTableJoinColumnName(Field field) {
        if (field.isAnnotationPresent(ManyToMany.class)) {
            ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
            if (manyToMany.mappedBy().isEmpty()) {
                return Optional.ofNullable(field.getAnnotation(JoinTable.class))
                        .map(JoinTable::inverseJoinColumn)
                        .map(JoinColumn::name)
                        .orElseGet(() -> defaultInverseTableJoinColumnName(field));
            }
        }
        return null;
    }


    /**
     * Constructs a default inverse join column name for a @ManyToMany association based on the type of the collection's generic entity class and its primary key column name.
     *
     * @param field The field representing the association.
     * @return The default inverse join column name.
     */
    public static String defaultInverseTableJoinColumnName(Field field) {
        Class<?> collectionGenericType = getCollectionGenericType(field);
        String columnIdName = columnIdName(collectionGenericType);
        return JOIN_TABLE_NAME_PATTERN.formatted(collectionGenericType.getSimpleName().toLowerCase(), columnIdName);
    }

    /**
     * Retrieves the database type of the join column in the join table for the specified field and entity class.
     *
     * @param field       The field representing the association.
     * @param entityClass The entity class containing the association.
     * @return The database type of the join column if applicable, otherwise null.
     */
    public static String joinColumnJoinTableDatabaseType(Field field, Class<?> entityClass) {
        if (field.isAnnotationPresent(ManyToMany.class)) {
            ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
            if (manyToMany.mappedBy().isEmpty()) {
                Class<?> typeOfIdField = getTypeOfIdField(entityClass);
                return convertToDatabaseType(typeOfIdField);
            }
        }
        return null;
    }

    /**
     * Retrieves the database type of the inverse join column in the join table for the specified field.
     *
     * @param field The field representing the association.
     * @return The database type of the inverse join column if applicable, otherwise null.
     */
    public static String inverseJoinColumnJoinTableDatabaseType(Field field) {
        if (field.isAnnotationPresent(ManyToMany.class)) {
            ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
            if (manyToMany.mappedBy().isEmpty()) {
                Class<?> collectionGenericType = EntityReflectionUtils.getCollectionGenericType(field);
                Class<?> typeOfIdField = getTypeOfIdField(collectionGenericType);
                return convertToDatabaseType(typeOfIdField);
            }
        }
        return null;
    }


    /**
     * Retrieves the foreign key constraint name for the join column in the join table associated with the specified field.
     *
     * @param field The field representing the association.
     * @return The foreign key constraint name for the join column if applicable, otherwise null.
     */
    public static String foreignKeyForJoinColumn(Field field) {
        if (field.isAnnotationPresent(ManyToMany.class)) {
            ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
            if (manyToMany.mappedBy().isEmpty()) {
                Optional<String> optionalKey = Optional.ofNullable(field.getAnnotation(JoinTable.class))
                        .flatMap(annotation -> Optional.ofNullable(annotation.foreignKey()))
                        .map(ForeignKey::name);
                String foreignKeyName;
                if (optionalKey.isEmpty() || optionalKey.get().isEmpty()) {
                    foreignKeyName = getForeignKeyConstraintName();
                } else {
                    foreignKeyName = optionalKey.get();
                }
                return foreignKeyName;
            }
        }
        return null;
    }

    /**
     * Retrieves the foreign key constraint name for the inverse join column in the join table associated with the specified field.
     *
     * @param field The field representing the association.
     * @return The foreign key constraint name for the inverse join column if applicable, otherwise null.
     */
    public static String foreignKeyForInverseJoinColumn(Field field) {
        if (field.isAnnotationPresent(ManyToMany.class)) {
            ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
            if (manyToMany.mappedBy().isEmpty()) {
                Optional<String> optionalKey = Optional.ofNullable(field.getAnnotation(JoinTable.class))
                        .flatMap(annotation -> Optional.ofNullable(annotation.inverseForeignKey()))
                        .map(ForeignKey::name);
                String foreignKeyName;
                if (optionalKey.isEmpty() || optionalKey.get().isEmpty()) {
                    foreignKeyName = getForeignKeyConstraintName();
                } else {
                    foreignKeyName = optionalKey.get();
                }
                return foreignKeyName;
            }
        }
        return null;
    }

    /**
     * Retrieves the name of the join column in the join table associated with the specified field.
     *
     * @param field The field representing the association.
     * @return The name of the join column if applicable, otherwise null.
     */
    public static String tableJoinColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(JoinTable.class))
                .map(JoinTable::joinColumn)
                .map(JoinColumn::name)
                .orElse(null);
    }

    /**
     * Retrieves the name of the join column corresponding to the specified source type and field type.
     *
     * @param sourceType The source entity type.
     * @param fieldType  The field type representing the association.
     * @return The name of the join column if found, otherwise null.
     */
    public static String joinColumnName(Class<?> sourceType, Class<?> fieldType) {
        return Arrays.stream(sourceType.getDeclaredFields())
                .filter(field -> fieldType.isAssignableFrom(field.getType())
                        || (isSupportedCollection(field) && fieldType.isAssignableFrom(getCollectionGenericType(field))))
                .map(EntityReflectionUtils::joinColumnName)
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves the name of the join column for the specified field.
     *
     * @param field The field representing the association.
     * @return The name of the join column if present, otherwise a default name based on the field name.
     */
    public static String joinColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(JoinColumn.class))
                .map(JoinColumn::name)
                .filter(Predicate.not(String::isEmpty))
                .orElse(getSnakeString(field.getName()).concat(ID_POSTFIX));
    }

    /**
     * Retrieves the database type for the join column in the specified field.
     *
     * @param field The field representing the association.
     * @return The database type for the join column if applicable, otherwise null.
     */
    public static String databaseTypeForJoinColumn(Field field) {
        Class<?> fieldType = field.getType();
        if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
            OneToOne oneToOne = field.getAnnotation(OneToOne.class);
            ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
            if (oneToOne != null && oneToOne.mappedBy().isEmpty()) {
                Class<?> idTypeOfRelation = getTypeOfIdField(fieldType);
                return convertToDatabaseType(idTypeOfRelation);
            } else if (manyToOne != null) {
                return convertToDatabaseType(getTypeOfIdField(fieldType));
            }
        }
        return null;
    }


    /**
     * Retrieves the type of the field annotated with @Id within the specified class.
     *
     * @param fieldType The class type to search for the @Id annotation.
     * @return The type of the field annotated with @Id if found, otherwise throws a MappingException.
     * @throws MappingException if no field with the @Id annotation is found in the specified class.
     */
    private static Class<?> getTypeOfIdField(Class<?> fieldType) {
        return Arrays.stream(fieldType.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .map(Field::getType)
                .findFirst()
                .orElseThrow(() -> new MappingException(
                        "Can't find @Id annotation in class '%s'".formatted(fieldType.getSimpleName())));
    }

    /**
     * Retrieves the name of the column annotated with @Id in the specified entity class.
     *
     * @param entityClass     The class representing the entity.
     * @return The name of the column annotated with @Id if found, otherwise throws a MissingAnnotationException.
     * @throws MissingAnnotationException if no field with the @Id annotation is found in the specified entity class.
     */
    public static String columnIdName(Class<?> entityClass) {
        return findColumnNameByAnnotation(entityClass, Id.class, UNABLE_TO_GET_ID_NAME_FOR_ENTITY);
    }

    /**
     * Retrieves the name of the column annotated with @Version in the specified entity class.
     *
     * @param entityClass     The class representing the entity.
     * @return The name of the column annotated with @Version if found, otherwise throws a MissingAnnotationException.
     * @throws MissingAnnotationException if no field with the @Version annotation is found in the specified entity class.
     */
    public static String columnVersionName(Class<?> entityClass) {
        return findColumnNameByAnnotation(entityClass, Version.class, UNABLE_TO_GET_VERSION_NAME_FOR_ENTITY);
    }

    /**
     * Helper method to find the name of a column annotated with the specified annotation in the given entity class.
     *
     * @param entityClass      The class representing the entity.
     * @param annotationClass  The annotation class to search for.
     * @param errorMessage     The error message to use if the annotation is not found.
     * @return The name of the column annotated with the specified annotation if found, otherwise throws a MissingAnnotationException.
     * @throws MissingAnnotationException if no field with the specified annotation is found in the specified entity class.
     */
    private static String findColumnNameByAnnotation(Class<?> entityClass,
                                                     Class<? extends Annotation> annotationClass,
                                                     String errorMessage) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(annotationClass))
                .map(EntityReflectionUtils::columnName)
                .findFirst()
                .orElseThrow(() -> new MissingAnnotationException(
                        errorMessage.formatted(entityClass.getSimpleName())));
    }

    /**
     * Sets the version value to 1 if it is null for the specified entity or entities.
     *
     * @param entityClass The class representing the entity.
     * @param entity      The entity for which to set the version value to 1 if it is null.
     */
    public static void setVersionValueIfNull(Class<?> entityClass, Object entity) {
        setVersionValueIfNull(entityClass, Collections.singletonList(entity));
    }

    /**
     * Sets the version value to 1 if it is null for the specified entities.
     *
     * @param entityClass The class representing the entity.
     * @param entities    The collection of entities for which to set the version value to 1 if it is null.
     */
    public static void setVersionValueIfNull(Class<?> entityClass, Collection<?> entities) {
        if (isColumnVersionFound(entityClass)) {
            Arrays.stream(entityClass.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(Version.class))
                    .forEach(field -> entities.forEach(entity -> {
                        var value = getValueFromObject(entity, field);
                        if (Objects.isNull(value)) {
                            setValueForObject(entity, field, 1);
                        }
                    }));
        }
    }

    /**
     * Retrieves the version value from the specified entity.
     *
     * @param entityClass The class representing the entity.
     * @param entity      The entity from which to retrieve the version value.
     * @return The version value from the entity.
     */
    public static Object columnVersionValue(Class<?> entityClass, Object entity) {
        return findColumnValueByAnnotation(entityClass, Version.class, entity);
    }

    /**
     * Retrieves the value of a column annotated with the specified annotation from the given entity.
     *
     * @param entityClass     The class representing the entity.
     * @param annotationClass The class object representing the annotation.
     * @param entity          The entity from which to retrieve the column value.
     * @return The value of the column annotated with the specified annotation from the entity.
     * @throws MissingAnnotationException If the annotation is not found on any field of the entity class.
     */
    private static Object findColumnValueByAnnotation(Class<?> entityClass,
                                                      Class<? extends Annotation> annotationClass,
                                                      Object entity) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(annotationClass))
                .map(field -> getValueFromObject(entity, field))
                .findFirst()
                .orElseThrow(() -> new MissingAnnotationException(
                        UNABLE_TO_GET_VERSION_NAME_FOR_ENTITY.formatted(entityClass.getSimpleName())));
    }

    /**
     * Checks if the version column is found in the specified entity class.
     *
     * @param entityClass The class representing the entity.
     * @return True if the version column is found, otherwise false.
     */
    public static boolean isColumnVersionFound(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(Version.class));
    }

    /**
     * Retrieves the value of the column annotated with the @Id annotation from the given entity.
     *
     * @param entityClass The class representing the entity.
     * @param entity      The entity from which to retrieve the column value.
     * @return The value of the column annotated with the @Id annotation from the entity.
     * @throws MissingAnnotationException If the @Id annotation is not found on any field of the entity class.
     */
    public static Object columnIdValue(Class<?> entityClass, Object entity) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(field -> getValueFromObject(entity, field))
                .findFirst()
                .orElseThrow(() -> new MissingAnnotationException(
                        UNABLE_TO_GET_ID_NAME_FOR_ENTITY.formatted(entityClass.getSimpleName())));
    }

    /**
     * Retrieves the type of the column annotated with the @Id annotation in the specified entity class.
     *
     * @param entityClass The class representing the entity.
     * @return The type of the column annotated with the @Id annotation.
     * @throws MissingAnnotationException If the @Id annotation is not found on any field of the entity class.
     */
    public static Class<?> columnIdType(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(Field::getType)
                .findFirst()
                .orElseThrow(() -> new MissingAnnotationException(
                        UNABLE_TO_GET_ID_NAME_FOR_ENTITY.formatted(entityClass.getSimpleName())));
    }

    /**
     * Retrieves the value of the specified field from the given object.
     *
     * @param field The field from which to retrieve the value.
     * @param obj   The object from which to retrieve the field value.
     * @return The value of the specified field in the object.
     * @throws BibernateGeneralException If an error occurs while accessing the field value.
     */
    public static Object getFieldValue(Field field, Object obj) {
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalAccessException exe) {
            throw new BibernateGeneralException("Unable to get [%s] field value for entity [%s], message [%s]"
                    .formatted(field.getName(), obj.getClass(), exe.getMessage()));
        }
    }


    /**
     * Retrieves the value of the specified field from the given object.
     *
     * @param entity The object from which to retrieve the field value.
     * @param field  The field from which to retrieve the value.
     * @return The value of the specified field in the object.
     */
    @SneakyThrows
    public static Object getValueFromObject(Object entity, Field field) {
        field.setAccessible(true);
        if (isToOneReference(field)) {
            Object reference = field.get(entity);
            if(reference != null && !reference.getClass().getName().contains("$$")) {
                return getIdValueFromField(reference);
            }
        }
        return field.get(entity);
    }

    /**
     * Checks if the specified field is annotated with @ManyToOne or @OneToOne.
     *
     * @param field The field to check.
     * @return True if the field is annotated with @ManyToOne or @OneToOne, false otherwise.
     */
    private static boolean isToOneReference(Field field) {
        return field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class);
    }

    /**
     * Retrieves the value of the ID field from the given object.
     *
     * @param reference The object from which to retrieve the ID value.
     * @return The value of the ID field in the object.
     */
    @SneakyThrows
    public static Object getIdValueFromField(Object reference) {
        Field referenceIdField = getIdField(reference.getClass());
        referenceIdField.setAccessible(true);
        return referenceIdField.get(reference);
    }

    /**
     * Verifies whether the ID field of the given reference object has a strategy generator or a non-null value.
     *
     * @param reference The reference object to verify.
     * @param <T>       The type of the reference object.
     */
    @SneakyThrows
    public static <T> void verifyIsIdHasStrategyGeneratorOrNotNullValue(T reference) {
        Field referenceIdField = getIdField(reference.getClass());
        if (!referenceIdField.isAnnotationPresent(GeneratedValue.class)) {
            referenceIdField.setAccessible(true);
            Object idValue = referenceIdField.get(reference);
            if (Objects.isNull(idValue)) {
                throw new BibernateValidationException(
                        ENTITY_S_SHOULD_HAVE_ID_THAT_NOT_NULL_OR_ADD_ANNOTATION_GENERATED_VALUE.formatted(reference.getClass().getSimpleName())
                );
            }
        }
    }

    /**
     * Verifies whether the ID field of each reference object in the collection has a strategy generator or a non-null value.
     *
     * @param references The collection of reference objects to verify.
     * @param <T>        The type of the reference objects in the collection.
     */
    @SneakyThrows
    public static <T> void verifyIsIdHasStrategyGeneratorOrNotNullValue(Collection<T> references) {
        references.stream()
                .map(reference -> Objects.requireNonNull(reference, ENTITY_MUST_BE_NOT_NULL))
                .forEach(EntityReflectionUtils::verifyIsIdHasStrategyGeneratorOrNotNullValue);
    }

    /**
     * Sets the value of the specified field in the given object using reflection.
     *
     * @param entity The object whose field's value needs to be set.
     * @param field  The field whose value needs to be set.
     * @param value  The value to set.
     */
    @SneakyThrows
    public static void setValueForObject(Object entity, Field field, Object value) {
        field.setAccessible(true);
        field.set(entity, value);
    }

    /**
     * Retrieves a value from a ResultSet based on the specified column name.
     *
     * @param resultSet       The ResultSet from which to retrieve the value.
     * @param joinColumnName  The name of the column from which to retrieve the value.
     * @return The value retrieved from the ResultSet.
     * @throws BibernateGeneralException If an error occurs while retrieving the value from the ResultSet.
     */
    public static Object getValueFromResultSetByColumn(ResultSet resultSet, String joinColumnName) {
        try {
            return resultSet.getObject(joinColumnName);
        } catch (SQLException e) {
            throw new BibernateGeneralException(String.format("Cannot get result from ResultSet by columnName = %s",
                    joinColumnName), e);
        }
    }

    /**
     * Retrieves a value from a ResultSet based on the specified field type and field name.
     *
     * @param field      The field object representing the field type.
     * @param resultSet  The ResultSet from which to retrieve the value.
     * @param fieldName  The name of the field from which to retrieve the value.
     * @return The value retrieved from the ResultSet.
     */
    public static Object getValueFromResultSet(Field field, ResultSet resultSet, String fieldName) {
        try {
            return resultSet.getObject(fieldName, field.getType());
        } catch (SQLException e) {
            log.warn("Cannot set [{}]", field.getName(), e);
        }

        return null;
    }

    /**
     * Retrieves the ID value of an entity from a ResultSet based on the field representing the entity's ID.
     *
     * @param field      The field representing the ID of the entity.
     * @param resultSet  The ResultSet containing the entity data.
     * @return The ID value of the entity, or null if not found or if an SQL exception occurs.
     */
    public static Object getEntityId(Field field, ResultSet resultSet) {
        try {
            var idFieldName = columnIdName(field.getDeclaringClass());
            return resultSet.getObject(idFieldName);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Compares two lists of ColumnSnapshot objects representing entity snapshots and returns the differences.
     *
     * @param currentEntitySnapshot  The list of ColumnSnapshot objects representing the current entity snapshot.
     * @param oldEntitySnapshot      The list of ColumnSnapshot objects representing the old entity snapshot.
     * @return A list of ColumnSnapshot objects representing the differences between the two snapshots.
     */
    public static List<ColumnSnapshot> getDifference(List<ColumnSnapshot> currentEntitySnapshot,
                                                     List<ColumnSnapshot> oldEntitySnapshot) {
        return IntStream.range(0, currentEntitySnapshot.size())
                .filter(i -> !Objects.equals(currentEntitySnapshot.get(i), oldEntitySnapshot.get(i)))
                .mapToObj(currentEntitySnapshot::get)
                .toList();
    }

    /**
     * Sets the value of a field in an object.
     *
     * @param field The field to set.
     * @param obj   The object whose field value will be set.
     * @param value The value to set in the field.
     * @throws BibernateGeneralException If an error occurs while setting the field value.
     */
    public static void setField(Field field, Object obj, Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new BibernateGeneralException(String.format("Cannot set %s", field.getName()), e);
        }
    }

    /**
     * Casts the primary key value to the appropriate type for an entity's ID field.
     *
     * @param entityClass The class of the entity.
     * @param primaryKey  The primary key value to cast.
     * @param <T>         The type of the entity ID.
     * @return The primary key value casted to the appropriate entity ID type.
     * @throws NullPointerException If either entityClass or primaryKey is null.
     */
    public static <T> T castIdToEntityId(Class<T> entityClass, Object primaryKey) {
        Objects.requireNonNull(entityClass, "entityClass must be not null");
        Objects.requireNonNull(primaryKey, "PrimaryKey must be not null");

        var fieldIdType = columnIdType(entityClass);

        if (!fieldIdType.isInstance(primaryKey)) {
            primaryKey = convertToType(primaryKey, fieldIdType);
        }

        return (T) primaryKey;
    }

    /**
     * Retrieves the generic type of a collection field.
     *
     * @param field The field representing the collection.
     * @return The class representing the generic type of the collection.
     * @throws BibernateGeneralException If the field is not a supported collection type.
     */
    public static Class<?> getCollectionGenericType(Field field) {
        if (isSupportedCollection(field)) {
            var parametrizedType = (ParameterizedType) field.getGenericType();
            return (Class<?>) parametrizedType.getActualTypeArguments()[0];
        }

        throw new BibernateGeneralException(
                "Unable to get Collection generic type for a field that is not a supported Collection. Field type: [%s]"
                        .formatted(field.getType()));
    }

    /**
     * Checks if a field represents a supported collection type.
     *
     * @param field The field to check.
     * @return True if the field represents a supported collection type; false otherwise.
     */
    public static boolean isSupportedCollection(Field field) {
        return List.class.isAssignableFrom(field.getType());
    }

    /**
     * Retrieves fields of an entity that can be used during insert operations.
     * These are fields that do not have special annotations like @GeneratedValue, @OneToOne without @JoinColumn,
     * ManyToOne without @JoinColumn, @OneToMany, @CreationTimestamp, @ManyToMany, or @UpdateTimestamp.
     *
     * @param entityClass The class representing the entity.
     * @return A list of fields suitable for insert operations.
     */
    public static List<Field> getInsertEntityFields(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
            .filter(Predicate.not(field ->
                    (field.isAnnotationPresent(GeneratedValue.class) && IDENTITY.equals(field.getAnnotation(GeneratedValue.class).strategy()))
                || (field.isAnnotationPresent(OneToOne.class) && !field.isAnnotationPresent(JoinColumn.class))
                || (field.isAnnotationPresent(ManyToOne.class) && !field.isAnnotationPresent(JoinColumn.class))
                || (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(CreationTimestamp.class)
                || (field.isAnnotationPresent(ManyToMany.class))
                    || field.isAnnotationPresent(UpdateTimestamp.class)))
            )
            .toList();
    }


    /**
     * Retrieves a list of EntityColumn objects representing fields of the given entity class.
     *
     * @param entityClass The class representing the entity.
     * @return A list of EntityColumn objects containing information about the entity fields.
     */
    public static List<EntityColumn> getEntityFields(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .map(field -> new EntityColumn(field.getName(), columnName(field)))
                .toList();
    }

    /**
     * Retrieves the field annotated with @Id in the given entity class.
     *
     * @param entityClass The class representing the entity.
     * @return The field annotated with @Id.
     * @throws MissingAnnotationException If no field annotated with @Id is found in the entity class.
     */
    public static Field getIdField(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new MissingAnnotationException(
                        UNABLE_TO_GET_ID_FIELD_FOR_ENTITY.formatted(entityClass.getSimpleName())));
    }

    /**
     * Retrieves fields annotated with @ManyToMany and @JoinTable in the given entity class.
     *
     * @param entityClass The class representing the entity.
     * @return A list of fields annotated with @ManyToMany and @JoinTable.
     */
    public static List<Field> getManyToManyWithJoinTableFields(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ManyToMany.class)
                        && field.isAnnotationPresent(JoinTable.class))
                .toList();
    }

    /**
     * Sets the value of the field annotated with @Id in the given entity object.
     *
     * @param entity The entity object.
     * @param value  The value to set.
     * @return The modified entity object.
     */
    public static Object setIdField(Object entity, Object value) {
        Field idField = getIdField(entity.getClass());
        setField(idField, entity, value);
        return entity;
    }

    /**
     * Retrieves the field annotated with @GeneratedValue in the given entity object.
     *
     * @param entity The entity object.
     * @return The field annotated with @GeneratedValue.
     * @throws MissingAnnotationException If no field annotated with @GeneratedValue is found in the entity object.
     */
    public static Field getGeneratedValueField(Object entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(GeneratedValue.class))
                .findFirst()
                .orElseThrow(() -> new MissingAnnotationException(
                        UNABLE_TO_GET_GENERATED_VALUE_FIELD_FOR_ENTITY.formatted(
                                entity.getClass().getSimpleName())));

    }

    /**
     * Retrieves the sequence configuration for a field annotated with @GeneratedValue and strategy set to "SEQUENCE"
     * in the given entity class.
     *
     * @param entityClass The class of the entity.
     * @param tableName   The name of the table associated with the sequence.
     * @return The sequence configuration.
     * @throws MissingAnnotationException If no such field with the specified sequence strategy is found in the entity class.
     */
    public static SequenceConf getGeneratedValueSequenceConfig(Class<?> entityClass,
                                                               String tableName) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(GeneratedValue.class)
                        && SEQUENCE.equals(field.getAnnotation(GeneratedValue.class).strategy()))
                .map(field -> getSequenceConfFromField(field, tableName))
                .findFirst()
                .orElseThrow(() -> new MissingAnnotationException(
                        CANNOT_FIND_SEQUENCE_STRATEGY.formatted(entityClass.getSimpleName())));

    }

    /**
     * Checks if a field has a specific annotation present.
     *
     * @param field            The field to check.
     * @param annotationClass  The annotation class to check for.
     * @return True if the field has the specified annotation; otherwise, false.
     */
    public static boolean isAnnotationPresent(Field field,
                                              Class<? extends Annotation> annotationClass) {
        return field.isAnnotationPresent(annotationClass);
    }

    /**
     * Retrieves the sequence configuration from a field annotated with @GeneratedValue and @SequenceGenerator
     * if the generator name matches the sequence generator name. Otherwise, it constructs a default sequence name
     * based on the table and column name.
     *
     * @param field     The field annotated with @GeneratedValue and @SequenceGenerator.
     * @param tableName The name of the table associated with the sequence.
     * @return The sequence configuration.
     */
    private static SequenceConf getSequenceConfFromField(Field field, String tableName) {
        var generatorName = field.getAnnotation(GeneratedValue.class).generator();
        if (!generatorName.isEmpty() && generatorName.equals(field.getAnnotation(SequenceGenerator.class).name())) {
            var sequenceName = field.getAnnotation(SequenceGenerator.class).sequenceName();
            var initialValue = field.getAnnotation(SequenceGenerator.class).initialValue();
            var allocationSize = field.getAnnotation(SequenceGenerator.class).allocationSize();
            return new SequenceConf(sequenceName, initialValue, allocationSize);
        }

        var columnName = columnName(field);
        return new SequenceConf(SequenceConf.DEFAULT_SEQ_TEMPLATE.formatted(tableName, columnName));
    }

    /**
     * Checks if a field is part of a bidirectional relationship by inspecting its annotations.
     *
     * @param field The field to check for bidirectionality.
     * @return True if the field is part of a bidirectional relationship; otherwise, false.
     */
    public static boolean isBidirectional(Field field) {
        return !field.getAnnotation(OneToOne.class).mappedBy().isBlank() ||
                Arrays.stream(field.getType().getDeclaredFields())
                        .map(bidirectionalField -> bidirectionalField.getAnnotation(OneToOne.class))
                        .filter(Objects::nonNull)
                        .map(OneToOne::mappedBy)
                        .anyMatch(mappedByName -> mappedByName.equals(field.getName()));
    }

    /**
     * Converts a value to the specified target type. Supports conversion from Number to numeric types,
     * Boolean to Boolean, Character to Character, and String to Long.
     *
     * @param value      The value to be converted.
     * @param targetType The target type to which the value will be converted.
     * @return The converted value.
     */
    private static Object convertToType(Object value, Class<?> targetType) {
        if (value instanceof Number number) {
            if (targetType.equals(Byte.class)) {
                return number.byteValue();
            } else if (targetType.equals(Short.class)) {
                return number.shortValue();
            } else if (targetType.equals(Integer.class)) {
                return number.intValue();
            } else if (targetType.equals(Long.class)) {
                return number.longValue();
            } else if (targetType.equals(Float.class)) {
                return number.floatValue();
            } else if (targetType.equals(Double.class)) {
                return number.doubleValue();
            }
        } else if (value instanceof Boolean && targetType.equals(Boolean.class)) {
            return value;
        } else if (value instanceof Character && targetType.equals(Character.class)) {
            return value;
        } else if (value instanceof String valueString && targetType.equals(Long.class)) {
            return Long.valueOf(valueString);
        }
        // Add more conditions for other types if needed
        return value;
    }

    /**
     * Converts a given string to snake case.
     *
     * @param str The input string.
     * @return The string converted to snake case.
     */
    public static String getSnakeString(String str) {
        return str.replaceAll(SNAKE_REGEX, REPLACEMENT).toLowerCase();
    }
}
