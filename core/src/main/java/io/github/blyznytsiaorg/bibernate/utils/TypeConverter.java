package io.github.blyznytsiaorg.bibernate.utils;

import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityColumnDetails;
import io.github.blyznytsiaorg.bibernate.exception.UnsupportedDataTypeException;
import lombok.experimental.UtilityClass;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class TypeConverter {
    private static final Map<Class<?>, String> typeToDatabaseTypeString = new HashMap<>();

    static {
        typeToDatabaseTypeString.put(Long.class, "bigint");
        typeToDatabaseTypeString.put(long.class, "bigint");
        typeToDatabaseTypeString.put(BigInteger.class, "bigint");
        typeToDatabaseTypeString.put(Integer.class, "integer");
        typeToDatabaseTypeString.put(int.class, "integer");
        typeToDatabaseTypeString.put(Short.class, "smallint");
        typeToDatabaseTypeString.put(short.class, "smallint");
        typeToDatabaseTypeString.put(Byte.class, "tinyint");
        typeToDatabaseTypeString.put(byte.class, "tinyint");
        typeToDatabaseTypeString.put(Float.class, "real");
        typeToDatabaseTypeString.put(float.class, "real");
        typeToDatabaseTypeString.put(Double.class, "double precision");
        typeToDatabaseTypeString.put(double.class, "double precision");
        typeToDatabaseTypeString.put(Boolean.class, "boolean");
        typeToDatabaseTypeString.put(boolean.class, "boolean");
        typeToDatabaseTypeString.put(Character.class, "char");
        typeToDatabaseTypeString.put(char.class, "char");
        typeToDatabaseTypeString.put(String.class, "varchar(255)");
        typeToDatabaseTypeString.put(BigDecimal.class, "numeric");
        typeToDatabaseTypeString.put(LocalDate.class, "date");
        typeToDatabaseTypeString.put(LocalTime.class, "time");
        typeToDatabaseTypeString.put(OffsetTime.class, "time");
        typeToDatabaseTypeString.put(LocalDateTime.class, "timestamp");
        typeToDatabaseTypeString.put(OffsetDateTime.class, "timestamp");
    }

    public static String convertToDatabaseType(Class<?> columnType) {
        return typeToDatabaseTypeString.get(columnType);
    }

    public static boolean isInternalJavaTypeSuitableForCreation(EntityColumnDetails entityColumn,
                                                                String tableName) {
        Class<?> fieldType = entityColumn.getFieldType();
        if (typeToDatabaseTypeString.get(fieldType) == null) {
            throw new UnsupportedDataTypeException("Error creating SQL commands for DDL creation "
                    + "for table '%s' [column type '%s' is not supported]"
                    .formatted(tableName, fieldType.getSimpleName()));
        }
        return true;
    }

    public static String getPostgresIdTypeForGeneration(Class<?> idFieldType, String tableName) {
        checkIdTypeForGeneration(idFieldType, tableName);
        if (idFieldType.equals(Integer.class) || idFieldType.equals(int.class)) {
            return "serial";
        } else if (idFieldType.equals(Long.class) || idFieldType.equals(long.class)) {
            return "bigserial";
        }
        return null;
    }

    public static void checkIdTypeForGeneration(Class<?> idFieldType, String tableName) {
        if (!idFieldType.equals(Long.class) && !idFieldType.equals(long.class)
                && !idFieldType.equals(Integer.class) && !idFieldType.equals(int.class)) {
            throw new UnsupportedDataTypeException("Error creating SQL commands for DDL creation "
                    + "for table '%s' [illegal identity column type '%s' for id generation]"
                    .formatted(tableName, idFieldType.getSimpleName()));
        }
    }
}