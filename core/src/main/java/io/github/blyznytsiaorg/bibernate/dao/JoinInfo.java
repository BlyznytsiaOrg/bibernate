package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents information about a join operation in a Bibernate query.
 * This class includes details about the joined table and the metadata of both the parent and child entities involved in the join.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class JoinInfo {

    /**
     * The name of the joined table.
     */
    @EqualsAndHashCode.Exclude
    private String joinedTable;

    /**
     * The metadata of the parent entity involved in the join.
     */
    private EntityMetadata parentEntityMetadata;

    /**
     * The metadata of the child entity involved in the join.
     */
    private EntityMetadata childEntityMetadata;
}
