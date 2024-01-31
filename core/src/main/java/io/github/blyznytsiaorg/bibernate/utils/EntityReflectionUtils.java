package io.github.blyznytsiaorg.bibernate.utils;

import static io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.GenerationType.*;

import io.github.blyznytsiaorg.bibernate.annotation.*;
import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.GenerationType;
import io.github.blyznytsiaorg.bibernate.entity.ColumnSnapshot;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.exception.MissingAnnotationException;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@UtilityClass
public class EntityReflectionUtils {

  public static final String UNABLE_TO_GET_ID_NAME_FOR_ENTITY = "Unable to get id name for entity [%s]";
  public static final String UNABLE_TO_GET_ID_FIELD_FOR_ENTITY = "Unable to get id field for entity [%s]";
  public static final String UNABLE_TO_GET_GENERATED_VALUE_FIELD_FOR_ENTITY = "Unable to get generated value field for entity [%s]";


  private static final String SNAKE_REGEX = "([a-z])([A-Z]+)";
  private static final String REPLACEMENT = "$1_$2";
  public static final String ID_POSTFIX = "_id";


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

  public static String joinColumnName(Field field) {
    return Optional.ofNullable(field.getAnnotation(JoinColumn.class))
        .map(JoinColumn::name)
        .filter(Predicate.not(String::isEmpty))
        .orElse(getSnakeString(field.getName()).concat(ID_POSTFIX));
  }

  public static String columnIdName(Class<?> entityClass) {
    return Arrays.stream(entityClass.getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(Id.class))
        .map(EntityReflectionUtils::columnName)
        .findFirst()
        .orElseThrow(() -> new MissingAnnotationException(
            UNABLE_TO_GET_ID_NAME_FOR_ENTITY.formatted(entityClass.getSimpleName())));
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

  public static Object getValueFromResultSetByColumn(ResultSet resultSet, String joinColumnName) {
    try {
      return resultSet.getObject(joinColumnName);
    } catch (SQLException e) {
      throw new BibernateGeneralException(
          String.format("Cannot get result from ResultSet by columnName = %s",
              joinColumnName), e);
    }
  }

  public static Object getValueFromResultSet(Field field, ResultSet resultSet, String fieldName) {
    try {
      return resultSet.getObject(fieldName, field.getType());
    } catch (SQLException e) {
      throw new BibernateGeneralException(String.format("Cannot set %s", field.getName()), e);
    }
  }

  public static List<ColumnSnapshot> getDifference(List<ColumnSnapshot> currentEntitySnapshot,
      List<ColumnSnapshot> oldEntitySnapshot) {
    return IntStream.range(0, currentEntitySnapshot.size())
        .filter(i -> !Objects.equals(currentEntitySnapshot.get(i), oldEntitySnapshot.get(i)))
        .mapToObj(currentEntitySnapshot::get)
        .collect(Collectors.toList());
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

  public static List<Field> getInsertEntityFieldsOld(Object entity) {
    return Arrays.stream(entity.getClass().getDeclaredFields())
        .filter(Predicate.not(field -> field.isAnnotationPresent(Id.class)))
        .filter(field -> Objects.nonNull(getValueFromObject(entity, field)))
        .toList();
  }

  public static List<Field> getInsertEntityFields(Object entity) {
    return Arrays.stream(entity.getClass().getDeclaredFields())
        .filter(Predicate.not(field -> field.isAnnotationPresent(GeneratedValue.class)
            && IDENTITY.equals(field.getAnnotation(GeneratedValue.class).strategy())))
        //.filter(field -> Objects.nonNull(getValueFromObject(entity, field)))
        //TODO: ADD utility jdbc class to insert all types or null
        .toList();
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

  public static boolean isRegularField(Field field) {
    return !isEntityField(field);
  }

  public static boolean isEntityField(Field field) {
    return field.isAnnotationPresent(OneToOne.class);
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


  private String getSnakeString(String str) {
    return str.replaceAll(SNAKE_REGEX, REPLACEMENT).toLowerCase();
  }
}
