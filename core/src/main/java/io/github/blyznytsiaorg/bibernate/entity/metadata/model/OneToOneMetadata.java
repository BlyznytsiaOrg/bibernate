package io.github.blyznytsiaorg.bibernate.entity.metadata.model;

import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class OneToOneMetadata {

    private List<CascadeType> cascadeTypes;
}
