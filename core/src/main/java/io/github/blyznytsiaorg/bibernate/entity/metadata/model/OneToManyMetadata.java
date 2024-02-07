package io.github.blyznytsiaorg.bibernate.entity.metadata.model;

import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class OneToManyMetadata {
    private String mappedByJoinColumnName;

    private List<CascadeType> cascadeTypes;
}