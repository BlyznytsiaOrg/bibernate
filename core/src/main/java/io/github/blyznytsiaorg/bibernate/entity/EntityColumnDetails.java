package io.github.blyznytsiaorg.bibernate.entity;

import io.github.blyznytsiaorg.bibernate.annotation.FetchType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EntityColumnDetails {
    private String fieldName;
    private String fieldColumnName;
    private boolean columnId;
    private Class<?> fieldType;
    private boolean oneToOne;
    private FetchType fetchType;
    private boolean joinColumn;
    private String joinColumnName;
    private String joinColumnTableName;
    private boolean manyToOne;
}
