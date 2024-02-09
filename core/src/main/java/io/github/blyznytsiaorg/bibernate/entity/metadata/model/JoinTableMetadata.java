package io.github.blyznytsiaorg.bibernate.entity.metadata.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class JoinTableMetadata {
    private String name;
    private String joinColumn;
    private String joinColumnDatabaseType;
    private String inverseJoinColumn;
    private String inverseJoinColumnDatabaseType;
    private String foreignKey;
    private String inverseForeignKey;
}
