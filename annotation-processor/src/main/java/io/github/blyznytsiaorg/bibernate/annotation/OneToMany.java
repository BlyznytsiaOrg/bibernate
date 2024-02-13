package io.github.blyznytsiaorg.bibernate.annotation;

import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a One-to-Many association between entities.
 * The annotated field represents the "one" side of the association,
 * while the referenced entity represents the "many" side.
 * 
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToMany {

    /**
     * The field that owns the relationship. Required unless the relationship is unidirectional.
     * 
     * @return the field that owns the relationship
     */
    String mappedBy() default "";

    /**
     * Defines the cascade operations to be applied to the associated entities when operations are performed on the owning entity.
     *
     * @return the cascade operations to be applied
     */
    CascadeType[] cascade() default {};
}
