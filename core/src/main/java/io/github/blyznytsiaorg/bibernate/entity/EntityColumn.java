package io.github.blyznytsiaorg.bibernate.entity;

/**
 * Represents a mapping between a field name and its corresponding column name in an entity.
 * This record encapsulates the information needed to map fields to database columns.
 *
 * @param fieldName       The name of the field in the entity class.
 * @param fieldColumnName The name of the column in the database table.
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public record EntityColumn(String fieldName, String fieldColumnName) {
}
