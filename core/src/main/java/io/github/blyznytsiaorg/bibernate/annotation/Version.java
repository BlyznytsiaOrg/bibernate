package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated field represents a version attribute used for optimistic locking.
 * Optimistic locking allows multiple transactions to access the same data simultaneously
 * with the assumption that conflicts are rare.
 * When an entity is updated, the version attribute is checked to detect concurrent modifications.
 *
 * Example usage:
 *
 * {@code
 * public class Product {
 *     @Version
 *     private int version;
 *     private String name;
 *     private double price;
 *
 *     // Getters and setters omitted for brevity
 * }
 * }
 *
 * In this example, the 'version' field of the 'Product' class is annotated with '@Version'.
 * When instances of 'Product' are updated, the value of the 'version' field is automatically
 * incremented, allowing the system to detect concurrent modifications and prevent data loss.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Version {
}
