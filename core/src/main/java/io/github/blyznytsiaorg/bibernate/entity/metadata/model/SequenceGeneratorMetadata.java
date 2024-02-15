package io.github.blyznytsiaorg.bibernate.entity.metadata.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents metadata for a sequence generator used in entity mapping.
 * It includes attributes such as name, sequence name, initial value and allocation size.
 *
 * @see io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Getter
@Setter
@AllArgsConstructor
public class SequenceGeneratorMetadata {
    private String name;
    private String sequenceName;
    private int initialValue;
    private int allocationSize;
}
