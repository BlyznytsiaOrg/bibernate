package io.github.blyznytsiaorg.bibernate.entity.metadata;

import io.github.blyznytsiaorg.bibernate.entity.metadata.model.IndexMetadata;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class EntityMetadata {
    private final String tableName;
    private final boolean immutable;
    private final boolean dynamicUpdate;
    private final Class<?> type;
    private final List<IndexMetadata> indexMetadatas;
    private final List<EntityColumnDetails> entityColumns;


    public EntityMetadata(String tableName, boolean immutable, boolean dynamicUpdate, Class<?> type) {
        this.tableName = tableName;
        this.immutable = immutable;
        this.dynamicUpdate = dynamicUpdate;
        this.type = type;
        this.indexMetadatas = new ArrayList<>();
        this.entityColumns = new ArrayList<>();
    }

    public void addIndexMetadata(List<IndexMetadata> indexMetadata) {
        indexMetadatas.addAll(indexMetadata);
    }

    public void addEntityColumn(EntityColumnDetails entityColumn) {
        entityColumns.add(entityColumn);
    }

    public List<EntityColumnDetails> getCascadeRemoveRelations() {
        return entityColumns.stream()
                .filter(this::isRemoveCascadeType)
                .toList();
    }

    private boolean isRemoveCascadeType(EntityColumnDetails column) {
        return Optional.ofNullable(column.getCascadeTypes())
                .map(types -> types.stream().anyMatch(type -> type == CascadeType.REMOVE || type == CascadeType.ALL))
                .orElse(false);
    }
}
