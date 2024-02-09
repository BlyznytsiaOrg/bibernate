package io.github.blyznytsiaorg.bibernate.entity.metadata.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class JoinColumnMetadata {
    private String name;
    private String databaseType;
    private String foreignKeyName;
}
