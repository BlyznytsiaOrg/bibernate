package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated class represents a database table.
 *
 * Example usage:
 *
 * {@code
 * \@Table(name = "products", indexes = {
 *     \@Index(name = "idx_product_name", columnList = "name"),
 *     \@Index(name = "idx_product_category", columnList = "category_id")
 * })
 * public class Product {
 *     // Class implementation omitted for brevity
 * }
 * }
 *
 * In this example, the 'Product' class is annotated with '@Table' to indicate that it
 * represents a database table named 'products'. Additionally, two indexes are defined
 * for the 'products' table: one on the 'name' column and another on the 'category_id' column.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {

    /**
     * Specifies the name of the table.
     *
     * @return the name of the table
     */
    String name() default "";

    /**
     * (Optional) Indexes for the table.  These are only used if
     * table generation is in effect.  Note that it is not necessary
     * to specify an index for a primary key, as the primary key
     * index will be created automatically.
     *
     * @return an array of indexes for the table
     */
    Index[] indexes() default {};

}
