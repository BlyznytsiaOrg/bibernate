package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the mapping of an entity attribute to a database column.
 * <p>
 * This annotation is used to map a Java class field to a database column. It allows customization of various column
 * properties such as name, uniqueness, nullability, and column definition.
 * <p>
 * Example usage:
 * <pre>{@code
 *   @Entity
 *   public class Entity {
 *       @Column(name = "first_name", unique = true, nullable = false, columnDefinition = "VARCHAR(255)")
 *       private String firstName;
 *       // Other fields and methods
 *   }
 * }</pre>
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    /**
     * (Optional) The name of the column in the database table.
     *
     * @return The name of the column.
     */
    String name() default "";

    /**
     * (Optional) Whether the column is a unique key.
     *
     * @return Whether the column is a unique key.
     */
    boolean unique() default false;

    /**
     * (Optional) Whether the database column is nullable.
     *
     * @return Whether the column allows NULL values.
     */
    boolean nullable() default true;

    /**
     * (Optional) The SQL fragment that is used when generating the DDL for the column.
     *
     * @return The custom SQL fragment.
     */
    String columnDefinition() default "";

}
