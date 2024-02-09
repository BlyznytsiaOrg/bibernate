package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as the identifier (primary key) of an entity class. This annotation is typically applied to a field
 * within a Java class that represents an entity in a data store. The presence of this annotation signals that the
 * annotated field is used to uniquely identify instances of the entity class.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id {

}
