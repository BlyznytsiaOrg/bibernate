package io.github.blyznytsiaorg.bibernate.entity.metadata.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents metadata for a join column in a database table.
 *
 * @see io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Getter
@Setter
@Builder
public class JoinColumnMetadata {
    private String name;
    private String databaseType;
    private String foreignKeyName;
}
