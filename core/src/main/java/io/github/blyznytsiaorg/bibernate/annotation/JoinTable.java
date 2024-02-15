package io.github.blyznytsiaorg.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a join table for defining a many-to-many association between two entities.
 *
 * When using this annotation, you must specify the name of the join table, the join column,
 * the inverse join column, and optionally, foreign key constraints for both columns.
 *
 * The join table should only be specified on fields representing many-to-many associations,
 * and it cannot be used for other types of associations (e.g., one-to-many, many-to-one).
 *
 * @see ManyToMany
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
