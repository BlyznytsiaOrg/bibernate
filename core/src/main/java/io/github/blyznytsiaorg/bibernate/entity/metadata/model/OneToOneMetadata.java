package io.github.blyznytsiaorg.bibernate.entity.metadata.model;

import io.github.blyznytsiaorg.bibernate.annotation.FetchType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OneToOneMetadata {
    String joinedTable;
    FetchType fetchType;
    Class<?> parentClass;
    Class<?> childClass;
}
