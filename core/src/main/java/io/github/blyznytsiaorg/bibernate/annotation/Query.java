package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method represents a query.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Query {

    /**
     * Specifies the value of the query.
     *
     * @return the value of the query
     */
    String value();

    /**
     * Indicates whether the query is a native SQL query.
     *
     * @return true if the query is a native SQL query, false otherwise
     */
    boolean nativeQuery() default false;

    /**
     * Indicates whether the query is written in HQL (Hibernate Query Language).
     *
     * @return true if the query is written in HQL, false otherwise
     */
    boolean hql() default true;
}
