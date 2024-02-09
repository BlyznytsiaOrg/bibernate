package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Index {

    /**
     * (Optional) The name of the index; defaults to a provider-generated name.
     */
    String name() default "";

    /**
     * (Required) The names of the columns to be included in the index,
     * in order.
     */
    String columnList();
}
