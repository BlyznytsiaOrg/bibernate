package io.github.blyznytsiaorg.bibernate.entity.metadata.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GeneratedValueMetadata {
    private String strategy;
    private String generator;
}
