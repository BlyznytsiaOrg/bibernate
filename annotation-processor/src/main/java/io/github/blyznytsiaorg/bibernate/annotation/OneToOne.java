package io.github.blyznytsiaorg.bibernate.annotation;


import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a one-to-one relationship between two entities.
 * This annotation is used to configure the mapping between the owning side and the inverse side of the relationship.
 * <p>
 * Example usage:
 * <pre>{@code
 *   @Entity
 *   public class EntityA {
 *       @OneToOne(mappedBy = "entityA", cascade = CascadeType.DELETE, fetch = FetchType.LAZY)
 *       private EntityB entityB;
 *       // Other fields and methods
 *   }
 *
 *   @Entity
 *   public class EntityB {
 *       // No need to specify mappedBy when the relationship is unidirectional
 *       @OneToOne(cascade = CascadeType.DELETE, fetch = FetchType.LAZY)
 *       private EntityA entityA;
 *       // Other fields and methods
 *   }
 * }</pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToOne {

    /**
     * The name of the field in the inverse side of the relationship.
     * This is required unless the relationship is unidirectional.
     * Defaults to an empty string, indicating that the relationship is not mapped by another field.
     */
    String mappedBy() default "";

    /**
     * Defines the cascade behavior for the relationship.
     * By default, no cascading behavior is applied.
     */
    CascadeType[] cascade() default {};

    /**
     * Defines the fetching strategy used to load the related entity.
     * By default, eager fetching is used.
     */
    FetchType fetch() default FetchType.EAGER;
}
