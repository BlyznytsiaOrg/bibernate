package io.github.blyznytsiaorg.bibernate.ddl;

import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityColumnDetails;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@Getter
@Builder
public class DDLFieldMetadataHolder {
    private String tableName;
    private EntityColumnDetails columnDetails;
    private List<String> columnNameAndDatabaseTypeList;
    private Set<String> foreignNameConstraints;
    private Class<?> entityClass;
    Map<Class<?>, EntityMetadata> bibernateEntityMetadata;
}
