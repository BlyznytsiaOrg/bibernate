package io.github.blyznytsiaorg.bibernate.ddl.model;

import java.util.List;

public record TableMetadata(List<String> primaryKeyName, List<IndexData> indexData,
                            List<ForeignKey> foreignKeys, List<ColumnData> columnData) {
}
