package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as being ignored for entity mapping in the Bibernate framework. This annotation is typically applied
 * to a class within a Java class hierarchy that represents entities in a data store. When present, instances of the
 * annotated class will not be treated as entities during entity scanning and mapping processes.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IgnoreEntity {

}
