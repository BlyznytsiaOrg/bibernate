package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method represents a query.
 *
 * Example usage:
 *
 * {@code
 * public interface ProductRepository {
 *     \@Query("SELECT p FROM Product p WHERE p.category = ?")
 *     List<Product> findByCategory(@Param("category") String category);
 * }
 * }
 *
 * In this example, the 'findByCategory' method in the 'ProductRepository' interface is annotated
 * with '@Query' to indicate that it represents a query. The value of the annotation specifies
 * the HQL (Hibernate Query Language) query to be executed.
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
     * Indicates whether the query is written in HQL (Bibernate Query Language).
     *
     * @return true if the query is written in HQL, false otherwise
     */
    boolean bql() default true;
}
