package io.github.blyznytsiaorg.bibernate.entity.metadata.model;

import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;
import lombok.Builder;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents metadata for a One-to-One relationship between entities.
 * It includes information about the mappedBy attribute, cascade types, joined table, fetch type,
 * parent class and child class associated with the relationship.
 *
 * @see io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata
 * @see CascadeType
 * @see FetchType
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Getter
@Setter
@Builder
public class OneToOneMetadata {
    private String mappedBy;
    private List<CascadeType> cascadeTypes;
    String joinedTable;
    FetchType fetchType;
    Class<?> parentClass;
    Class<?> childClass;
}
