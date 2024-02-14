package io.github.blyznytsiaorg.bibernate.ddl;

import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityColumnDetails;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The DDLFieldMetadataHolder class represents metadata for a field in a Data Definition Language (DDL) query.
 * <p>
 * Instances of this class are typically used as parameters or context objects during the resolution of
 * field mappings and the generation of DDL queries.
 *
 * @see DDLQueryCreator
 * @see DDLProcessor
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
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
