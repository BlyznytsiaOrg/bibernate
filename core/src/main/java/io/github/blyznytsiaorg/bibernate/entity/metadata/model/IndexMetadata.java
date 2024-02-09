package io.github.blyznytsiaorg.bibernate.entity.metadata.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class IndexMetadata {

    private String name;

    private String columnList;
}
