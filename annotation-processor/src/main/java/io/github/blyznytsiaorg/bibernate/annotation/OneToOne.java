package io.github.blyznytsiaorg.bibernate.annotation;


import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToOne {

    /**
     * The field that owns the relationship. Required unless the relationship is unidirectional.
     */
    String mappedBy() default "";

    CascadeType[] cascade() default {};

    FetchType fetch() default FetchType.EAGER;
}