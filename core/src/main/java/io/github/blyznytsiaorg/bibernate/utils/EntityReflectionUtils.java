package io.github.blyznytsiaorg.bibernate.utils;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.identity.SequenceConf;
import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;
import io.github.blyznytsiaorg.bibernate.entity.EntityColumn;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.IndexMetadata;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
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
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static io.github.blyznytsiaorg.bibernate.annotation.GenerationType.IDENTITY;
import static io.github.blyznytsiaorg.bibernate.annotation.GenerationType.SEQUENCE;
import static io.github.blyznytsiaorg.bibernate.utils.DDLUtils.getForeignKeyConstraintName;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_FIND_SEQUENCE_STRATEGY;
import static io.github.blyznytsiaorg.bibernate.utils.TypeConverter.convertToDatabaseType;


/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
@UtilityClass
public class EntityReflectionUtils {
    public static final String JAVA_LANG = "java.lang";
    public static final String JAVA_MATH = "java.math";
    public static final String JAVA_SQL = "java.sql";
    public static final String JAVA_TIME = "java.time";
    private static final String UNABLE_TO_GET_ID_NAME_FOR_ENTITY = "Unable to get id name for entity [%s]";
    private static final String UNABLE_TO_GET_VERSION_NAME_FOR_ENTITY = "Unable to get version name for entity [%s]";
    public static final String UNABLE_TO_GET_ID_FIELD_FOR_ENTITY = "Unable to get id field for entity [%s]";
    public static final String UNABLE_TO_GET_GENERATED_VALUE_FIELD_FOR_ENTITY = "Unable to get generated value field for entity [%s]";

    private static final String SNAKE_REGEX = "([a-z])([A-Z]+)";
    private static final String REPLACEMENT = "$1_$2";
    private static final String ID_POSTFIX = "_id";
    public static final String JOIN_TABLE_NAME_PATTERN = "%s_%s";

    public static String table(Class<?> entityClass) {
        return Optional.ofNullable(entityClass.getAnnotation(Table.class))
                .map(Table::name)
                .filter(Predicate.not(String::isEmpty))
                .orElse(getSnakeString(entityClass.getSimpleName()));
    }

    public static boolean isImmutable(Class<?> entityClass) {
        return entityClass.isAnnotationPresent(Immutable.class);
    }

    public static boolean isDynamicUpdate(Class<?> entityClass) {
        return entityClass.isAnnotationPresent(DynamicUpdate.class);
    }

    public static boolean isColumnHasAnnotation(Field field,
                                                Class<? extends Annotation> annotationClass) {
        return field.isAnnotationPresent(annotationClass);
    }

    public static List<IndexMetadata> getIndexMetadata(Class<?> entityClass) {
        return Optional.ofNullable(entityClass.getAnnotation(Table.class))
                .map(table -> Arrays.stream(table.indexes())
                        .map(index -> new IndexMetadata(index.name(), index.columnList()))
                        .toList())
                .orElseGet(ArrayList::new);
    }

    public static String columnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(Column.class))
                .map(Column::name)
                .filter(Predicate.not(String::isEmpty))
                .or(() -> getJoinColumnName(field))
                .orElse(getSnakeString(field.getName()));
    }

    private static Optional<String> getJoinColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(JoinColumn.class))
                .map(JoinColumn::name)
                .filter(Predicate.not(String::isEmpty));
    }

    public static String databaseTypeForInternalJavaType(Field field) {
        if (isInternalJavaType(field)) {
            Column annotation = field.getAnnotation(Column.class);
            String columnDefinition = (annotation != null) ? annotation.columnDefinition() : "";
            return columnDefinition.isEmpty() ? convertToDatabaseType(field.getType()) : columnDefinition;
        }
        return null;
    }

    private boolean isInternalJavaType(Field field) {
        Class<?> fieldType = field.getType();
        String packageName = fieldType.getPackageName();
        return packageName.equals(JAVA_LANG) || packageName.equals(JAVA_MATH)
                || packageName.equals(JAVA_SQL) || packageName.equals(JAVA_TIME);
    }

    public static String mappedByJoinColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(OneToMany.class))
                .map(OneToMany::mappedBy)
                .filter(Predicate.not(String::isEmpty))
                .flatMap(mappedByName -> {
                    Class<?> collectionGenericType = getCollectionGenericType(field);

                    return getMappedByColumnName(mappedByName, collectionGenericType);
                })
                .orElse(joinColumnName(field));
    }

    private static Optional<String> getMappedByColumnName(String mappedByName,
                                                          Class<?> collectionGenericType) {
        return Arrays.stream(collectionGenericType.getDeclaredFields())
                .filter(f -> Objects.equals(f.getName(), mappedByName))
                .findFirst()
                .map(EntityReflectionUtils::joinColumnName);
    }

    public static String joinTableName(Field field) {
        return Optional.ofNullable(field.getAnnotation(JoinTable.class))
                .map(JoinTable::name)
                .filter(Predicate.not(String::isEmpty))
                .orElse(null);
    }
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

    private static String getManyManyDefaultTableName(Field field, Class<?> entityClass) {
        if(field.isAnnotationPresent(ManyToMany.class)) {
            Class<?> collectionGenericType = EntityReflectionUtils.getCollectionGenericType(field);
            String thisEntityTableName = table(entityClass);
            String relationEntityTableName = table(collectionGenericType);
            return JOIN_TABLE_NAME_PATTERN.formatted(thisEntityTableName, relationEntityTableName);
        }
        return null;
    }

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

    private static String defaultTableJoinColumnName(Class<?> entityClass) {
        String columnIdName = columnIdName(entityClass);
        return JOIN_TABLE_NAME_PATTERN.formatted(entityClass.getSimpleName().toLowerCase(), columnIdName);
    }
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

    public static String defaultInverseTableJoinColumnName(Field field) {
        Class<?> collectionGenericType = getCollectionGenericType(field);
        String columnIdName = columnIdName(collectionGenericType);
        return JOIN_TABLE_NAME_PATTERN.formatted(collectionGenericType.getSimpleName().toLowerCase(), columnIdName);
    }

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

    public static String tableJoinColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(JoinTable.class))
                .map(JoinTable::joinColumn)
                .map(JoinColumn::name)
                .orElse(null);
    }

    public static String joinColumnName(Class<?> sourceType, Class<?> fieldType) {
        return Arrays.stream(sourceType.getDeclaredFields())
          .filter(field -> fieldType.isAssignableFrom(field.getType())
            || (isSupportedCollection(field) && fieldType.isAssignableFrom(getCollectionGenericType(field))))
          .map(EntityReflectionUtils::joinColumnName)
          .findFirst()
          .orElse(null);
    }

    public static String joinColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(JoinColumn.class))
                .map(JoinColumn::name)
                .filter(Predicate.not(String::isEmpty))
                .orElse(getSnakeString(field.getName()).concat(ID_POSTFIX));
    }

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

    private static Class<?> getTypeOfIdField(Class<?> fieldType) {
        return Arrays.stream(fieldType.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .map(Field::getType)
                .findFirst()
                .orElseThrow(() -> new MappingException(
                        "Can't find @Id annotation in class '%s'".formatted(fieldType.getSimpleName())));
    }

    public static String columnIdName(Class<?> entityClass) {
        return findColumnNameByAnnotation(entityClass, Id.class, UNABLE_TO_GET_ID_NAME_FOR_ENTITY);
    }

    public static String columnVersionName(Class<?> entityClass) {
        return findColumnNameByAnnotation(entityClass, Version.class, UNABLE_TO_GET_VERSION_NAME_FOR_ENTITY);
    }

    private static String findColumnNameByAnnotation(Class<?> entityClass,
                                                     Class<? extends Annotation> annotationClass,
                                                     String errorMessage
    ) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(annotationClass))
                .map(EntityReflectionUtils::columnName)
                .findFirst()
                .orElseThrow(() -> new MissingAnnotationException(
                        errorMessage.formatted(entityClass.getSimpleName())));
    }

    public static void setVersionValueIfNull(Class<?> entityClass,
                                             Object entity) {
        setVersionValueIfNull(entityClass, Collections.singletonList(entity));
    }

    public static void setVersionValueIfNull(Class<?> entityClass,
                                             Collection<?> entities) {
        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Version.class))
                .forEach(field -> entities.forEach(entity -> {
                    var value = getValueFromObject(entity, field);
                    if (Objects.isNull(value)) {
                        setValueForObject(entity, field, 1);
                    }
                }));
    }

    public static Object columnVersionValue(Class<?> entityClass,
                                            Object entity) {
        return findColumnValueByAnnotation(entityClass, Version.class, entity);
    }

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

    public static boolean isColumnVersionFound(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(Version.class));

    }

    public static Object columnIdValue(Class<?> entityClass, Object entity) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(field -> getValueFromObject(entity, field))
                .findFirst()
                .orElseThrow(() -> new MissingAnnotationException(
                        UNABLE_TO_GET_ID_NAME_FOR_ENTITY.formatted(entityClass.getSimpleName())));
    }

    public static Class<?> columnIdType(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(Field::getType)
                .findFirst()
                .orElseThrow(() -> new MissingAnnotationException(
                        UNABLE_TO_GET_ID_NAME_FOR_ENTITY.formatted(entityClass.getSimpleName())));
    }

    public static Object getFieldValue(Field field, Object obj) {
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalAccessException exe) {
            throw new BibernateGeneralException("Unable to get [%s] field value for entity [%s], message [%s]"
              .formatted(field.getName(), obj.getClass(), exe.getMessage()));
        }
    }

    public static Field getEntityIdField(Object entity) {
        try {
            var columnIdName = columnIdName(entity.getClass());
            return entity.getClass().getDeclaredField(columnIdName);
        } catch (NoSuchFieldException exe) {
            throw new BibernateGeneralException("Unable to get id field for entity [%s], message [%s]"
              .formatted(entity.getClass(), exe.getMessage()));
        }
    }

    @SneakyThrows
    public static Object getValueFromObject(Object entity, Field field) {
        field.setAccessible(true);
        if (isToOneReference(field)) {
            Object reference = field.get(entity);
            if(!reference.getClass().getName().contains("$$")) {
                return getIdValueFromField(reference);
            }
        }
        return field.get(entity);
    }

    private static boolean isToOneReference(Field field) {
        return field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class);
    }

    @SneakyThrows
    private static Object getIdValueFromField(Object reference) {
        Field referenceIdField = getIdField(reference.getClass());
        referenceIdField.setAccessible(true);
        return referenceIdField.get(reference);
    }

    @SneakyThrows
    public static void setValueForObject(Object entity, Field field, Object value) {
        field.setAccessible(true);
        field.set(entity, value);
    }

    public static Object getValueFromResultSetByColumn(ResultSet resultSet, String joinColumnName) {
        try {
            return resultSet.getObject(joinColumnName);
        } catch (SQLException e) {
            throw new BibernateGeneralException(String.format("Cannot get result from ResultSet by columnName = %s",
                    joinColumnName), e);
        }
    }

    public static Object getValueFromResultSet(Field field, ResultSet resultSet, String fieldName) {
        try {
            return resultSet.getObject(fieldName, field.getType());
        } catch (SQLException e) {
            log.warn("Cannot set [{}]", field.getName(), e);
        }

        return null;
    }

    public static List<ColumnSnapshot> getDifference(List<ColumnSnapshot> currentEntitySnapshot,
                                                     List<ColumnSnapshot> oldEntitySnapshot) {
        return IntStream.range(0, currentEntitySnapshot.size())
                .filter(i -> !Objects.equals(currentEntitySnapshot.get(i), oldEntitySnapshot.get(i)))
                .mapToObj(currentEntitySnapshot::get)
                .toList();
    }

    public static void setField(Field field, Object obj, Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new BibernateGeneralException(String.format("Cannot set %s", field.getName()), e);
        }
    }

    public static <T> T castIdToEntityId(Class<T> entityClass, Object primaryKey) {
        Objects.requireNonNull(entityClass, "entityClass must be not null");
        Objects.requireNonNull(primaryKey, "PrimaryKey must be not null");

        var fieldIdType = columnIdType(entityClass);

        if (!fieldIdType.isInstance(primaryKey)) {
            primaryKey = convertToType(primaryKey, fieldIdType);
        }

        return (T) primaryKey;
    }

    public static Class<?> getCollectionGenericType(Field field) {
        if (isSupportedCollection(field)) {
            var parametrizedType = (ParameterizedType) field.getGenericType();
            return (Class<?>) parametrizedType.getActualTypeArguments()[0];
        }

        throw new BibernateGeneralException(
                "Unable to get Collection generic type for a field that is not a supported Collection. Field type: [%s]"
                        .formatted(field.getType()));
    }

    public static boolean isSupportedCollection(Field field) {
        return List.class.isAssignableFrom(field.getType());
    }

    public static List<Field> getInsertEntityFields(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
            .filter(Predicate.not(field ->
                    (field.isAnnotationPresent(GeneratedValue.class) && IDENTITY.equals(field.getAnnotation(GeneratedValue.class).strategy()))
                || (field.isAnnotationPresent(OneToOne.class) && !field.isAnnotationPresent(JoinColumn.class))
                || (field.isAnnotationPresent(OneToMany.class))
                    )
            )
            //.filter(field -> Objects.nonNull(getValueFromObject(entity, field)))
            //TODO: ADD utility jdbc class to insert all types or null
            .toList();
    }


    public static List<EntityColumn> getEntityFields(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .map(field -> new EntityColumn(field.getName(), columnName(field)))
                .toList();
    }

    public static Field getIdField(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(Id.class))
            .findFirst()
            .orElseThrow(() -> new MissingAnnotationException(
                UNABLE_TO_GET_ID_FIELD_FOR_ENTITY.formatted(entityClass.getSimpleName())));
    }

    public static Object setIdField(Object entity, Object value) {
        Field idField = getIdField(entity.getClass());
        setField(idField, entity, value);
        return entity;
    }

    public static Field getGeneratedValueField(Object entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(GeneratedValue.class))
            .findFirst()
            .orElseThrow(() -> new MissingAnnotationException(
                UNABLE_TO_GET_GENERATED_VALUE_FIELD_FOR_ENTITY.formatted(
                    entity.getClass().getSimpleName())));

    }

    public static Field getGeneratedValueSequenceStrategyField(Object entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(GeneratedValue.class)
                && SEQUENCE.equals(field.getAnnotation(GeneratedValue.class).strategy()))
            .findFirst()
            .orElseThrow(() -> new MissingAnnotationException(
                CANNOT_FIND_SEQUENCE_STRATEGY.formatted(entity.getClass().getSimpleName())));

    }

    public static SequenceConf getGeneratedValueSequenceConfig(Class<?> entityClass, String tableName) {
        return Arrays.stream(entityClass.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(GeneratedValue.class)
                && SEQUENCE.equals(field.getAnnotation(GeneratedValue.class).strategy()))
            .map(field -> getSequenceConfFromField(field, tableName))
            .findFirst()
            .orElseThrow(() -> new MissingAnnotationException(
                CANNOT_FIND_SEQUENCE_STRATEGY.formatted(entityClass.getSimpleName())));

    }

    public static boolean isAnnotationPresent(Field field, Class<? extends Annotation> annotationClass) {
        return field.isAnnotationPresent(annotationClass);
    }

    private static SequenceConf getSequenceConfFromField(Field field, String tableName) {
        var generatorName = field.getAnnotation(GeneratedValue.class).generator();
        if(!generatorName.isEmpty() && generatorName.equals(field.getAnnotation(SequenceGenerator.class).name())) {
            var sequenceName = field.getAnnotation(SequenceGenerator.class).sequenceName();
            var initialValue = field.getAnnotation(SequenceGenerator.class).initialValue();
            var allocationSize = field.getAnnotation(SequenceGenerator.class).allocationSize();
            return new SequenceConf(sequenceName, initialValue, allocationSize);
        }

        var columnName = columnName(field);
        return new SequenceConf(SequenceConf.DEFAULT_SEQ_TEMPLATE.formatted(tableName, columnName));
    }

    public static boolean isBidirectional(Field field) {
        return !field.getAnnotation(OneToOne.class).mappedBy().isBlank() ||
               Arrays.stream(field.getType().getDeclaredFields())
                       .map(bidirectionalField -> bidirectionalField.getAnnotation(OneToOne.class))
                       .filter(Objects::nonNull)
                       .map(OneToOne::mappedBy)
                       .anyMatch(mappedByName -> mappedByName.equals(field.getName()));
    }

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
        } else  if (value instanceof String valueString && targetType.equals(Long.class)) {
            return Long.valueOf(valueString);
        }
        // Add more conditions for other types if needed
        return value;
    }

    public static String getSnakeString(String str) {
        return str.replaceAll(SNAKE_REGEX, REPLACEMENT).toLowerCase();
    }
}
