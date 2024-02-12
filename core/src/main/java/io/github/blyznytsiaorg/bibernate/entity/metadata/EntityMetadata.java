package io.github.blyznytsiaorg.bibernate.entity.metadata;

import io.github.blyznytsiaorg.bibernate.annotation.enumeration.CascadeType;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;
import io.github.blyznytsiaorg.bibernate.dao.JoinInfo;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.IndexMetadata;
import lombok.Getter;

import java.util.*;

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

    public Set<JoinInfo> joinInfos(Class<?> entityClass, List<EntityColumnDetails> currentEntityColumns,
                                    Map<Class<?>, EntityMetadata> bibernateEntityMetadata, Set<Class<?>> trackVisitedClasses) {
        Set<JoinInfo> joinInfos = new LinkedHashSet<>();
        trackVisitedClasses.add(entityClass);

        for (var entityColumn : currentEntityColumns) {
            var oneToOneMetadata = entityColumn.getOneToOne();
            if (oneToOneMetadata != null &&
                    // EMPTY.equals(oneToOneMetadata.getMappedBy())
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

    public Set<EntityMetadata> getOneToOneEntities(Class<?> entityClass, List<EntityColumnDetails> currentEntityColumns,
                                                   Map<Class<?>, EntityMetadata> bibernateEntityMetadata, Set<Class<?>> trackVisitedClasses) {

        Set<EntityMetadata> oneToOneEntities = new HashSet<>();
        var searchedEntityMetadata = bibernateEntityMetadata.get(entityClass);
        oneToOneEntities.add(searchedEntityMetadata);

        trackVisitedClasses.add(entityClass);

        for (var entityColumnDetails : currentEntityColumns) {
            var oneToOneMetadata = entityColumnDetails.getOneToOne();
            if (Objects.nonNull(oneToOneMetadata) &&
                 // EMPTY.equals(oneToOneMetadata.getMappedBy())
                (EMPTY.equals(oneToOneMetadata.getMappedBy()) || oneToOneMetadata.getFetchType() == FetchType.EAGER)
            ) {
                var currentEntityClass = entityColumnDetails.getFieldType();
                var entityMetadata = bibernateEntityMetadata.get(currentEntityClass);
                oneToOneEntities.add(entityMetadata);

                if (!trackVisitedClasses.contains(currentEntityClass)) {
                    oneToOneEntities.addAll(
                            getOneToOneEntities(
                                    currentEntityClass, entityMetadata.getEntityColumns(),
                                    bibernateEntityMetadata, trackVisitedClasses
                            )
                    );
                }
            }
        }

        return oneToOneEntities;
    }
}
