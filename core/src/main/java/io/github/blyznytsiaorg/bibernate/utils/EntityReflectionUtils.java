package io.github.blyznytsiaorg.bibernate.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;
import io.github.blyznytsiaorg.bibernate.entity.EntityColumn;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.exception.MissingAnnotationException;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Slf4j
@UtilityClass
public class EntityReflectionUtils {

    public static final String UNABLE_TO_GET_ID_NAME_FOR_ENTITY = "Unable to get id name for entity [%s]";

    public static final String UNABLE_TO_GET_VERSION_NAME_FOR_ENTITY = "Unable to get version name for entity [%s]";

    private static final String SNAKE_REGEX = "([a-z])([A-Z]+)";
    private static final String REPLACEMENT = "$1_$2";
    public static final String ID_POSTFIX = "_id";


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

    public static boolean isColumnHasAnnotation(Field field, Class<? extends Annotation> annotationClass) {
        return field.isAnnotationPresent(annotationClass);
    }

    public static String columnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(Column.class))
                .map(Column::name)
                .filter(Predicate.not(String::isEmpty))
                .orElse(getSnakeString(field.getName()));
    }

    public static String mappedByCollectionJoinColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(OneToMany.class))
                .map(OneToMany::mappedBy)
                .filter(Predicate.not(String::isEmpty))
                .flatMap(mappedByName -> getMappedByColumnName(mappedByName, field))
                .orElse(joinColumnName(field));
    }

    public static String mappedByEntityJoinColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(OneToOne.class))
                .map(OneToOne::mappedBy)
                .filter(Predicate.not(String::isEmpty))
                .flatMap(mappedByName -> getMappedByColumnName(mappedByName, field))
                .orElse(joinColumnName(field));
    }

    private static Optional<String> getMappedByColumnName(String mappedByName, Field field) {
        var mappedByType = field.getType();
        if (EntityRelationsUtils.isCollectionField(field)) {
            mappedByType = getCollectionGenericType(field);
        }

        return Arrays.stream(mappedByType.getDeclaredFields())
                .filter(f -> Objects.equals(f.getName(), mappedByName))
                .findFirst()
                .map(EntityReflectionUtils::joinColumnName);
    }

    public static String joinColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(JoinColumn.class))
                .map(JoinColumn::name)
                .filter(Predicate.not(String::isEmpty))
                .orElse(getSnakeString(field.getName()).concat(ID_POSTFIX));
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
        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Version.class))
                .forEach(field -> {
                    Object value = getValueFromObject(entity, field);
                    if (Objects.isNull(value)) {
                        setValueForObject(entity, field, 1);
                    }
                });
    }

    public static Object columnVersionValue(Class<?> entityClass,
                                            Object entity) {
        return findColumnValueByAnnotation(entityClass, Version.class, entity);
    }

    private static Object findColumnValueByAnnotation(Class<?> entityClass, Class<? extends Annotation> annotationClass, Object entity) {
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

    @SneakyThrows
    public static Object getValueFromObject(Object entity, Field field) {
        field.setAccessible(true);
        return field.get(entity);
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

    public static List<Field> getInsertEntityFields(Object entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(Predicate.not(field -> field.isAnnotationPresent(Id.class)))
                .filter(field -> Objects.nonNull(getValueFromObject(entity, field)))
                .toList();
    }

    public static List<EntityColumn> getEntityFields(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .map(field -> new EntityColumn(field.getName(), columnName(field)))
                .collect(Collectors.toList());
    }

    public static boolean isBidirectionalOwnerSide(Field field) {
        return !field.getAnnotation(OneToOne.class).mappedBy().isBlank();
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
        } else if (value instanceof String valueString && targetType.equals(Long.class)) {
            return Long.valueOf(valueString);
        }
        // Add more conditions for other types if needed
        return value;
    }

//    public static Supplier<Object> createNewInstance(Constructor<?> constructor, Object[] args, Class<?> clazz,
//                                                     boolean lazy) {
//        return () -> {
//            try {
//                if (lazy) {
//                    return ProxyUtils.createProxy(clazz, constructor, args);
//                } else {
//                    return constructor.newInstance(args);
//                }
//            } catch (Exception e) {
//                throw new BibernateGeneralException("Cannot create proxy instance ", e);
//            }
//        };
//    }

    private String getSnakeString(String str) {
        return str.replaceAll(SNAKE_REGEX, REPLACEMENT).toLowerCase();
    }

}
