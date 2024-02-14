package io.github.blyznytsiaorg.bibernate.entity.metadata.model;

import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents metadata for a One-to-Many relationship between entities.
 * It includes information about the mappedBy join column name and cascade types associated with the relationship.
 *
 * @see io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata
 * @see CascadeType
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Getter
@Setter
@Builder
public class OneToManyMetadata {
    private String mappedByJoinColumnName;

    private List<CascadeType> cascadeTypes;
}
