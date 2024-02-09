package io.github.blyznytsiaorg.bibernate.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies a sequence generator for generating values for annotated fields marked with
 * {@link io.github.blyznytsiaorg.bibernate.annotation.GeneratedValue} using the {@link GenerationType#SEQUENCE} strategy.
 * This annotation is typically applied to a field, method, or class within a Java class that represents an entity in a data store.
 * <p>
 * The {@code name} attribute defines the name of the generator, while the {@code sequenceName} attribute specifies
 * the name of the database sequence to be used. The {@code initialValue} and {@code allocationSize} attributes allow
 * customization of the initial value and the increment size of the sequence, respectively.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Target(value = {TYPE, METHOD, FIELD})
@Retention(value = RUNTIME)
public @interface SequenceGenerator {

    /**
     * Specifies the name of the sequence generator.
     *
     * @return The name of the sequence generator.
     */
    String name();

    /**
     * Specifies the name of the database sequence to be used.
     *
     * @return The name of the database sequence.
     */
    String sequenceName();

    /**
     * Specifies the initial value for the sequence. The default is 1.
     *
     * @return The initial value for the sequence.
     */
    int initialValue() default 1;

    /**
     * Specifies the size of the allocation block for the sequence. The default is 50.
     *
     * @return The allocation size for the sequence.
     */
    int allocationSize() default 50;
}
