package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated entity is immutable, meaning its state cannot be changed after creation.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Immutable {
}
