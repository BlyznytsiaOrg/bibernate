package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a column for joining an entity association.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JoinColumn {

    /**
     * Specifies the name of the column.
     *
     * @return the name of the column
     */
    String name();

    /**
     * Specifies the foreign key constraint for the column.
     *
     * @return the foreign key constraint for the column
     */
    ForeignKey foreignKey() default @ForeignKey(name = "");
}
