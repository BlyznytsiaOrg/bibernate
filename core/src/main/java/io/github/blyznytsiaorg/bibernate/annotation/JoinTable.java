package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a join table for an entity association.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JoinTable {

    /**
     * Specifies the name of the join table.
     *
     * @return the name of the join table
     */
    String name();

    /**
     * Specifies the join column for the association.
     *
     * @return the join column for the association
     */
    JoinColumn joinColumn();

    /**
     * Specifies the inverse join column for the association.
     *
     * @return the inverse join column for the association
     */
    JoinColumn inverseJoinColumn();

    /**
     * Specifies the foreign key constraint for the join column.
     *
     * @return the foreign key constraint for the join column
     */
    ForeignKey foreignKey() default @ForeignKey(name = "");

    /**
     * Specifies the foreign key constraint for the inverse join column.
     *
     * @return the foreign key constraint for the inverse join column
     */
    ForeignKey inverseForeignKey() default @ForeignKey(name = "");
}
