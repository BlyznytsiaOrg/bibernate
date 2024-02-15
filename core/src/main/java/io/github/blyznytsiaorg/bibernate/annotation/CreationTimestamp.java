package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated field should be automatically populated with the timestamp of entity creation.
 * Example usage:
 *
 * <pre>{@code
 * public class ExampleEntity {
 *     @CreationTimestamp
 *     private OffsetDateTime createdAt;
 *
 *     // Getter and setter methods
 * }
 * }</pre>
 *
 * @see UpdateTimestamp
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CreationTimestamp {
}
