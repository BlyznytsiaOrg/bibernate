package io.github.blyznytsiaorg.bibernate.entity.metadata.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ColumnMetadata {
    private String name;
    private String databaseType;
    private boolean unique;
    private boolean nullable;
    private String columnDefinition;
    private boolean timestamp;
    private boolean timeZone;
}
