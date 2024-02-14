package io.github.blyznytsiaorg.bibernate.entity.metadata.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents metadata about a column in an entity, including its name, database type, uniqueness, nullability,
 * column definition, and other properties.
 *
 * @see io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Getter
@Setter
@Builder
public class ColumnMetadata {
    private String name;
    private String databaseType;
    private boolean unique;
    private boolean nullable;
    private String columnDefinition;
    private boolean timestamp;
    private boolean timeZone;
}
