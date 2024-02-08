package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class JoinInfo {
    private String joinedTable;
    private EntityMetadata parentEntityMetadata;
    private EntityMetadata childEntityMetadata;
}
