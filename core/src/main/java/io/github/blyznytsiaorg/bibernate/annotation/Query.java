package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Query {
    String value();

    boolean nativeQuery() default false;

    boolean hql() default true;
}
