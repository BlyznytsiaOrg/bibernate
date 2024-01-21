package io.github.blyznytsiaorg.bibernate.utils;

import io.github.blyznytsiaorg.bibernate.annotation.Column;
import io.github.blyznytsiaorg.bibernate.annotation.DynamicUpdate;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.Table;
import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.exception.MissingAnnotationException;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@UtilityClass
public class EntityReflectionUtils {

    private static final String SNAKE_REGEX = "([a-z])([A-Z]+)";
    private static final String REPLACEMENT = "$1_$2";
    public static final String UNABLE_TO_GET_ID_NAME_FOR_ENTITY_S = "Unable to get id name for entity [%s]";
    public static final String UNABLE_TO_GET_FIELD_NAMES_AND_THEIR_VALUES_FOR_ENTITY = "Unable to get field names and their values for entity %s";

    public static String table(Class<?> entityClass) {
        return Optional.ofNullable(entityClass.getAnnotation(Table.class))
                .map(Table::name)
                .filter(Predicate.not(String::isEmpty))
                .orElse(getSnakeString(entityClass.getSimpleName()));
    }

    public static boolean isDynamicUpdate(Class<?> entityClass) {
        return entityClass.isAnnotationPresent(DynamicUpdate.class);
    }

    public static String columnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(Column.class))
                .map(Column::name)
                .filter(Predicate.not(String::isEmpty))
                .orElse(getSnakeString(field.getName()));
    }

    public static String columnIdName(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(EntityReflectionUtils::columnName)
                .findFirst()
                .orElseThrow(() -> new MissingAnnotationException(
                        UNABLE_TO_GET_ID_NAME_FOR_ENTITY_S.formatted(entityClass.getSimpleName()))
                );
    }

    public static Object columnIdValue(Class<?> entityClass, Object entity) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(field -> getValueFromObject(entity, field))
                .findFirst()
                .orElseThrow(() -> new MissingAnnotationException(
                        UNABLE_TO_GET_ID_NAME_FOR_ENTITY_S.formatted(entityClass.getSimpleName()))
                );
    }

    public static Class<?> columnIdType(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(Field::getType)
                .findFirst()
                .orElseThrow(() -> new MissingAnnotationException(
                        UNABLE_TO_GET_ID_NAME_FOR_ENTITY_S.formatted(entityClass.getSimpleName()))
                );
    }

    @SneakyThrows
    public static Object getValueFromObject(Object entity, Field field) {
        field.setAccessible(true);
        return field.get(entity);
    }

    public <T> List<ColumnSnapshot> getFieldNamesToValues(T object) {
        Class<?> type = object.getClass();

        List<ColumnSnapshot> fieldNamesToValues = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            try {
                field.setAccessible(true);

                ColumnSnapshot fieldNameToValue = buildFieldNameToValue(field, object);
                fieldNamesToValues.add(fieldNameToValue);
            } catch (IllegalAccessException | IllegalArgumentException exe) {
                throw new BibernateGeneralException(
                        UNABLE_TO_GET_FIELD_NAMES_AND_THEIR_VALUES_FOR_ENTITY.formatted(type.getSimpleName()),
                        exe
                );
            }
        }

        return fieldNamesToValues;
    }

    private ColumnSnapshot buildFieldNameToValue(Field field, Object object) throws IllegalAccessException {
        String columnName = columnName(field);
        Object columnValue = field.get(object);

        return new ColumnSnapshot(columnName, columnValue, field.getType());
    }

    public static List<ColumnSnapshot> isCurrentSnapshotAndOldSnapshotTheSame(List<ColumnSnapshot> currentEntitySnapshot, List<ColumnSnapshot> oldEntitySnapshot) {
        return IntStream.range(0, currentEntitySnapshot.size())
                .filter(i -> !currentEntitySnapshot.get(i).equals(oldEntitySnapshot.get(i)))
                .mapToObj(currentEntitySnapshot::get)
                .collect(Collectors.toList());
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

    private String getSnakeString(String str) {
        return str.replaceAll(SNAKE_REGEX, REPLACEMENT).toLowerCase();
    }
}
