package io.github.blyznytsiaorg.bibernate.entity.metadata.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents metadata for an index associated with an entity.
 *
 * @see io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Getter
@Setter
@AllArgsConstructor
public class IndexMetadata {

    private String name;

    private String columnList;
}
