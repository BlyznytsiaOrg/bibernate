package io.github.blyznytsiaorg.bibernate.entity.metadata;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class EntityMetadata {
    private final String tableName;
    private final boolean immutable;
    private final boolean dynamicUpdate;
    private final Class<?> type;
    private final List<EntityColumnDetails> entityColumns;


    public EntityMetadata(String tableName, boolean immutable, boolean dynamicUpdate, Class<?> type) {
        this.tableName = tableName;
        this.immutable = immutable;
        this.dynamicUpdate = dynamicUpdate;
        this.type = type;
        this.entityColumns = new ArrayList<>();
    }

    public void addEntityColumn(EntityColumnDetails entityColumn) {
        entityColumns.add(entityColumn);
    }
}
