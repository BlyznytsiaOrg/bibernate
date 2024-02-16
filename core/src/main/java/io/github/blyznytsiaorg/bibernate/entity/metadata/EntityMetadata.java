package io.github.blyznytsiaorg.bibernate.entity.metadata;

import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;
import io.github.blyznytsiaorg.bibernate.dao.JoinInfo;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.IndexMetadata;
import lombok.Getter;

import java.util.*;

/**
 * Represents metadata for an entity, including table name, immutability, dynamic update,
 * entity type, index metadata and entity columns.
 *
 * @see EntityMetadataCollector
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Getter
public class EntityMetadata {
    private static final String NOT_SPECIFIED_ENTITY_ID_FOR_ENTITY = "Not specified entity Id for entity %s";
    private static final String EMPTY = "";

    private final String tableName;
    private final boolean immutable;
    private final boolean dynamicUpdate;
    private final Class<?> type;
    private final List<IndexMetadata> indexMetadatas;
    private final List<EntityColumnDetails> entityColumns;

    /**
     * Constructs an EntityMetadata object with the specified table name, immutability,
     * dynamic update and entity type.
     *
     * @param tableName     The name of the table associated with the entity.
     * @param immutable     A boolean indicating whether the entity is immutable.
     * @param dynamicUpdate A boolean indicating whether dynamic update is enabled for the entity.
     * @param type          The class representing the entity type.
     */

    public EntityMetadata(String tableName, boolean immutable, boolean dynamicUpdate, Class<?> type) {
        this.tableName = tableName;
        this.immutable = immutable;
        this.dynamicUpdate = dynamicUpdate;
        this.type = type;
        this.indexMetadatas = new ArrayList<>();
        this.entityColumns = new ArrayList<>();
    }

    /**
     * Adds a list of index metadata to the entity metadata.
     *
     * @param indexMetadata The list of index metadata to add.
     */
    public void addIndexMetadata(List<IndexMetadata> indexMetadata) {
        indexMetadatas.addAll(indexMetadata);
    }

    /**
     * Adds an entity column to the entity metadata.
     *
     * @param entityColumn The entity column to add.
     */
    public void addEntityColumn(EntityColumnDetails entityColumn) {
        entityColumns.add(entityColumn);
    }

    /**
     * Retrieves the entity columns with cascade remove relations.
     *
     * @return A list of entity columns with cascade remove relations.
     */
    public List<EntityColumnDetails> getCascadeRemoveRelations() {
        return entityColumns.stream()
                .filter(this::isRemoveCascadeType)
                .toList();
    }

    /**
     * Checks if the cascade type for the specified entity column includes REMOVE or ALL.
     *
     * @param column The entity column details to check.
     * @return true if the cascade type includes REMOVE or ALL, otherwise false.
     */
    private boolean isRemoveCascadeType(EntityColumnDetails column) {
        return Optional.ofNullable(column.getCascadeTypes())
                .map(types -> types.stream().anyMatch(type -> type == CascadeType.REMOVE || type == CascadeType.ALL))
                .orElse(false);
    }

    /**
     * Retrieves the join information for the specified entity class and its associated entity columns.
     *
     * @param entityClass           The entity class to retrieve join information for.
     * @param currentEntityColumns  The list of entity columns to analyze.
     * @param bibernateEntityMetadata  The metadata map containing entity metadata.
     * @param trackVisitedClasses  The set of visited classes to avoid cyclic dependencies.
     * @return The set of join information for the specified entity class.
     */
    public Set<JoinInfo> joinInfos(Class<?> entityClass, List<EntityColumnDetails> currentEntityColumns,
                                    Map<Class<?>, EntityMetadata> bibernateEntityMetadata, Set<Class<?>> trackVisitedClasses) {
        var joinInfos = new LinkedHashSet<JoinInfo>();
        trackVisitedClasses.add(entityClass);

        for (var entityColumn : currentEntityColumns) {
            var oneToOneMetadata = entityColumn.getOneToOne();
            if (oneToOneMetadata != null &&
                (EMPTY.equals(oneToOneMetadata.getMappedBy()) || oneToOneMetadata.getFetchType() == FetchType.EAGER)
            ) {
                var build = JoinInfo.builder()
                        .joinedTable(oneToOneMetadata.getJoinedTable())
                        .childEntityMetadata(bibernateEntityMetadata.get(oneToOneMetadata.getChildClass()))
                        .parentEntityMetadata(bibernateEntityMetadata.get(oneToOneMetadata.getParentClass()))
                        .build();
                joinInfos.add(build);

                var currentEntityClass = entityColumn.getFieldType();
                var searchedEntityMetadata = bibernateEntityMetadata.get(currentEntityClass);

                if (!trackVisitedClasses.contains(currentEntityClass)) {
                    joinInfos.addAll(joinInfos(currentEntityClass, searchedEntityMetadata.getEntityColumns(), bibernateEntityMetadata, trackVisitedClasses));
                }
            }
        }
        return joinInfos;
    }
}
