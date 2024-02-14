package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated parameter is a named parameter for a method or constructor.
 *
 * Example usage:
 *
 * {@code
 * public interface UserRepository extends BibernateRepository<User, Long>{
 *     User findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
 * }
 * }
 *
 * In this example, the 'findByUsernameAndPassword' method in the 'UserRepository' interface is
 * annotated with '@Param' for each parameter, indicating that they are named parameters.
 * These named parameters are then used in the query method.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

    /**
     * Specifies the name of the parameter.
     *
     * @return the name of the parameter
     */
    String value();
}
