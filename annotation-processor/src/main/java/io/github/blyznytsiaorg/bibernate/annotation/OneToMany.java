package io.github.blyznytsiaorg.bibernate.annotation;

import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToMany {

    /**
     * The field that owns the relationship. Required unless the relationship is unidirectional.
     */
    String mappedBy() default "";

    CascadeType[] cascade() default {};
}
