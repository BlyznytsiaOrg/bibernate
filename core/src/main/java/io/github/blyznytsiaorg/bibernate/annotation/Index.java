package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated entity should have an index created on specified columns.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Index {

    /**
     * (Optional) The name of the index; defaults to a provider-generated name.
     * 
     * @return the name of the index
     */
    String name() default "";

    /**
     * (Required) The names of the columns to be included in the index, in order.
     * 
     * @return the column list for the index
     */
    String columnList();
}
