package io.github.blyznytsiaorg.bibernate.entity.metadata.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents metadata for a generated value in an entity, indicating that the associated field's value is generated
 * automatically upon entity creation.
 *
 * @see io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Getter
@Setter
@AllArgsConstructor
public class GeneratedValueMetadata {
    private String strategy;
    private String generator;
}
