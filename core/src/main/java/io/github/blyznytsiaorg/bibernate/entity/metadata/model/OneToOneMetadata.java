package io.github.blyznytsiaorg.bibernate.entity.metadata.model;

import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;
import lombok.Builder;
import io.github.blyznytsiaorg.bibernate.annotation.FetchType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class OneToOneMetadata {
    private String  mappedBy;

    private List<CascadeType> cascadeTypes;
    String joinedTable;
    FetchType fetchType;
    Class<?> parentClass;
    Class<?> childClass;
}
