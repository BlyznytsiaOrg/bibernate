package io.github.blyznytsiaorg.bibernate.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class EntityMetadata {
    private final String tableName;
    private final String entityIdColumnName;
    private final boolean immutable;
    private final boolean dynamicUpdate;
    private final List<EntityColumnDetails> entityColumns;

    public EntityMetadata(String tableName,String entityIdColumnName, boolean immutable, boolean dynamicUpdate) {
        this.tableName = tableName;
        this.entityIdColumnName = entityIdColumnName;
        this.immutable = immutable;
        this.dynamicUpdate = dynamicUpdate;
        this.entityColumns = new ArrayList<>();

    }

    public void addEntityColumn(EntityColumnDetails entityColumn) {
        entityColumns.add(entityColumn);
    }
}
