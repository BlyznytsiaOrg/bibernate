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

/**
 * Contains utility methods for converting Java types to database types and checking compatibility of types for DDL creation.
 * This utility class provides methods to convert Java types to their corresponding database types and to check if a given Java type
 * is suitable for creating columns in a database table.
 *
 * @see io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadataCollector
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
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

    /**
     * Converts a Java type to its corresponding database type.
     *
     * @param columnType the Java type to convert
     * @return the corresponding database type
     */
    public static String convertToDatabaseType(Class<?> columnType) {
        return typeToDatabaseTypeString.get(columnType);
    }

    /**
     * Checks if the internal Java type is suitable for creating database columns.
     * Throws an exception if the type is not supported.
     *
     * @param entityColumn the details of the entity column
     * @param tableName    the name of the table
     * @return true if the Java type is suitable, otherwise false
     * @throws UnsupportedDataTypeException if the Java type is not supported
     */
    public static boolean isInternalJavaTypeSuitableForCreation(EntityColumnDetails entityColumn,
                                                                String tableName) {
        var fieldType = entityColumn.getFieldType();
        if (typeToDatabaseTypeString.get(fieldType) == null) {
            throw new UnsupportedDataTypeException("Error creating SQL commands on DDL creation "
                    + "for table '%s' [column type '%s' is not supported]"
                    .formatted(tableName, fieldType.getSimpleName()));
        }
        return true;
    }

    /**
     * Gets the Postgres ID type for generation based on the Java ID field type.
     *
     * @param idFieldType the type of the ID field
     * @param tableName   the name of the table
     * @return the Postgres ID type for generation ("serial" or "bigserial")
     * @throws UnsupportedDataTypeException if the ID field type is not supported for generation
     */
    public static String getPostgresIdTypeForGeneration(Class<?> idFieldType, String tableName) {
        checkIdTypeForGeneration(idFieldType, tableName);
        if (idFieldType.equals(Integer.class) || idFieldType.equals(int.class)) {
            return "serial";
        } else if (idFieldType.equals(Long.class) || idFieldType.equals(long.class)) {
            return "bigserial";
        }
        return null;
    }

    /**
     * Checks if the ID field type is suitable for ID generation in SQL commands.
     *
     * @param idFieldType the type of the ID field
     * @param tableName   the name of the table
     * @throws UnsupportedDataTypeException if the ID field type is not supported for generation
     */
    public static void checkIdTypeForGeneration(Class<?> idFieldType, String tableName) {
        if (!idFieldType.equals(Long.class) && !idFieldType.equals(long.class)
                && !idFieldType.equals(Integer.class) && !idFieldType.equals(int.class)) {
            throw new UnsupportedDataTypeException("Error creating SQL commands on DDL creation "
                    + "for table '%s' [illegal identity column type '%s' for id generation]. Supported types: '%s', '%s', '%s', '%s'"
                    .formatted(tableName, idFieldType.getSimpleName(),
                            Integer.class.getName(), int.class.getName(),
                            Long.class.getName(), long.class.getName()));
        }
    }
}