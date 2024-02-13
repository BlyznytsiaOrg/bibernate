package io.github.blyznytsiaorg.bibernate.annotation;

import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify a many-to-many relationship between entities.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManyToMany {

    /**
     * (Optional) The name of the field on the target entity that owns the relationship.
     *
     * @return The name of the field that owns the relationship.
     */
    String mappedBy() default "";

    /**
     * (Optional) Specifies the cascade operations to be applied to the relationship.
     *
     * @return The cascade operations.
     */
    CascadeType[] cascade() default {};
}
