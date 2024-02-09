package io.github.blyznytsiaorg.bibernate.annotation;

import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManyToOne {

  CascadeType[] cascade() default {};
}
