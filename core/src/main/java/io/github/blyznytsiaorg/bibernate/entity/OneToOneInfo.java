package io.github.blyznytsiaorg.bibernate.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OneToOneInfo {
    private EntityMetadata childEntityMetadata;
    private EntityMetadata parentEntityMetadata;
}
