package io.github.blyznytsiaorg.bibernate.entity.metadata.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SequenceGeneratorMetadata {
    private String name;
    private String sequenceName;
    private int initialValue;
    private int allocationSize;
}
