package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class JoinInfo {
    @EqualsAndHashCode.Exclude
    private String joinedTable;
    private EntityMetadata parentEntityMetadata;
    private EntityMetadata childEntityMetadata;
}
