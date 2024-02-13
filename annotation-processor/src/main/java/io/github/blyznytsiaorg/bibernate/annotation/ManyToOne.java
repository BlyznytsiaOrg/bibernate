package io.github.blyznytsiaorg.bibernate.annotation;

import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a Many-to-One association between entities.
 * The annotated field represents the "many" side of the association,
 * while the referenced entity represents the "one" side.
 * 
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManyToOne {

  /**
   * Defines the cascade operations to be applied to the associated entity when operations are performed on the owning entity.
   *
   * @return the cascade operations to be applied
   */
  CascadeType[] cascade() default {};

  /**
   * Defines the fetching strategy to be used when retrieving the associated entity.
   *
   * @return the fetching strategy
   */
  FetchType fetch() default FetchType.EAGER;
}
