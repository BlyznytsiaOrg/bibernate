package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated entity should use dynamic update behavior, 
 * where only modified fields are included in the SQL update statement.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DynamicUpdate {
}
