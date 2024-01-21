package io.github.blyznytsiaorg.bibernate.entity;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public record ColumnSnapshot(String name, Object value, Class<?> type) { }
