package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated field represents a timestamp that should be updated automatically upon entity modification.
 * Example usage:
 *
 * <pre>{@code
 * public class ExampleEntity {
 *     @UpdateTimestamp
 *     private LocalDateTime updatedAt;
 *
 *     // Getter and setter methods
 * }
 * }</pre>
 *
 * @see CreationTimestamp
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UpdateTimestamp {
}
