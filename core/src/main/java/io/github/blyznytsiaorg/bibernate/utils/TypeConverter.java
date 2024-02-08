package io.github.blyznytsiaorg.bibernate.utils;

import io.github.blyznytsiaorg.bibernate.exception.MappingException;
import io.github.blyznytsiaorg.bibernate.exception.UnsupportedDataTypeException;
import lombok.experimental.UtilityClass;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@UtilityClass
public class TypeConverter {

    public static String convertToDatabaseType(Class<?> columnType) {
        String databaseType;
        if (columnType.equals(Long.class) || columnType.equals(long.class) || columnType.equals(BigInteger.class)) {
            databaseType = "bigint";
        } else if (columnType.equals(Integer.class) || columnType.equals(int.class)) {
            databaseType = "integer";
        } else if (columnType.equals(Short.class) || columnType.equals(short.class)) {
            databaseType = "smallint";
        } else if (columnType.equals(Byte.class) || columnType.equals(byte.class)) {
            databaseType = "tinyint";
        } else if (columnType.equals(Float.class) || columnType.equals(float.class)) {
            databaseType = "real";
        } else if (columnType.equals(Double.class) || columnType.equals(double.class)) {
            databaseType = "double precision";
        } else if (columnType.equals(Boolean.class) || columnType.equals(boolean.class)) {
            databaseType = "boolean";
        } else if (columnType.equals(Character.class) || columnType.equals(char.class)) {
            databaseType = "char";
        } else if (columnType.equals(String.class)) {
            databaseType = "varchar(255)";
        } else if (columnType.equals(BigDecimal.class)) {
            databaseType = "numeric";
        } else if (columnType.equals(Date.class) || columnType.equals(LocalDate.class)) {
            databaseType = "date";
        } else if (columnType.equals(Time.class) || columnType.equals(LocalTime.class)) {
            databaseType = "time";
        } else if (columnType.equals(LocalDateTime.class)) {
            databaseType = "timestamp";
        } else {
            throw new UnsupportedDataTypeException("The type %s is not supported for ddl creation"
                    .formatted(columnType.getSimpleName()));
        }
        return databaseType;
    }

    public static String getPostgresIdType(Class<?> idFieldType, String tableName) {
        String idFieldPostgresType;
        if (idFieldType.equals(Integer.class) || idFieldType.equals(int.class)) {
            idFieldPostgresType = "serial";
        } else if (idFieldType.equals(Long.class) || idFieldType.equals(long.class)) {
            idFieldPostgresType = "bigserial";
        } else {
            throw new MappingException("Error creating SQL commands for "
                    + "table '%s' [illegal identity column type '%s']"
                    .formatted(tableName, idFieldType.getSimpleName()));
        }
        return idFieldPostgresType;
    }
}