package io.github.blyznytsiaorg.bibernate.entity;

/**
 * Represents a snapshot of a column in a data representation.
 * Each snapshot contains the name, value, and type of the column.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public record ColumnSnapshot(String name, Object value, Class<?> type) { }
