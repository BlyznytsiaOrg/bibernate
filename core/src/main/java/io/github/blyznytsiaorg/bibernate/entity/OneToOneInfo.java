package io.github.blyznytsiaorg.bibernate.entity;

import lombok.Getter;

@Getter
public class OneToOneInfo {
    private EntityMetadata childEntityMetadata;
    private EntityMetadata parentEntityMetadata;
    private String getJoinedTableName;
}
