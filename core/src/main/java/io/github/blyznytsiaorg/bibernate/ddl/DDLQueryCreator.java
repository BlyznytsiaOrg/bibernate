package io.github.blyznytsiaorg.bibernate.ddl;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getSnakeString;

import io.github.blyznytsiaorg.bibernate.ddl.field.FieldResolver;
import io.github.blyznytsiaorg.bibernate.ddl.field.ManyToManyFieldResolver;
import io.github.blyznytsiaorg.bibernate.ddl.field.NoRelationFieldResolver;
import io.github.blyznytsiaorg.bibernate.ddl.field.ToOneRelationFieldResolver;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.IndexMetadata;
import io.github.blyznytsiaorg.bibernate.exception.MappingException;
import io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder;
import io.github.blyznytsiaorg.bibernate.utils.DDLUtils;
import lombok.Getter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

@Getter
public class DDLQueryCreator {
    public static final String DROP_TABLE = "drop table if exists %s cascade";
    public static final String CREATE_INDEX = "create index %s on %s (%s)";
    public static final String DELIMITER = ", ";
    public static final String CREATE_TABLE = "create table %s (";
    public static final String BRACKET = ")";
    private final List<String> dropSequences = new ArrayList<>();
    private final List<String> createSequences = new ArrayList<>();
    private final List<String> createTables = new ArrayList<>();
    private final List<String> createIndexes = new ArrayList<>();
    private final List<String> createConstraints = new ArrayList<>();
    private final List<String> dropConstraints = new ArrayList<>();
    private final List<String> dropTables = new ArrayList<>();
    private final Map<Class<?>, EntityMetadata> bibernateEntityMetadata;
    private final Map<Integer, List<String>> ddlMetadata = new TreeMap<>();
    private final List<FieldResolver> fieldResolvers = new ArrayList<>();

    public DDLQueryCreator() {
        bibernateEntityMetadata = BibernateContextHolder.getBibernateEntityMetadata();
        ddlMetadata.put(OperationOrder.DROP_CONSTRAINT, dropConstraints);
        ddlMetadata.put(OperationOrder.DROP_TABLE, dropTables);
        ddlMetadata.put(OperationOrder.DROP_SEQUENCE, dropSequences);
        ddlMetadata.put(OperationOrder.CREATE_SEQUENCE, createSequences);
        ddlMetadata.put(OperationOrder.CREATE_TABLE, createTables);
        ddlMetadata.put(OperationOrder.CREATE_INDEX, createIndexes);
        ddlMetadata.put(OperationOrder.CREATE_CONSTRAINT, createConstraints);
        fieldResolvers.addAll(List.of(new NoRelationFieldResolver(),
                new ToOneRelationFieldResolver(),
                new ManyToManyFieldResolver()));
        createQueries();
    }

    public void createQueries() {

        bibernateEntityMetadata.forEach((entityClass, entityMetadata) -> {

            Set<String> foreignNameConstraints = new HashSet<>();
            createIndexQuery(entityMetadata);
            String tableName = entityMetadata.getTableName();

            ddlMetadata.computeIfAbsent(OperationOrder.DROP_TABLE, k -> new ArrayList<>())
                    .add(DROP_TABLE.formatted(tableName));

            StringBuilder builder = new StringBuilder(CREATE_TABLE.formatted(tableName));
            List<String> columnNameAndDatabaseTypeList = new ArrayList<>();

            entityMetadata.getEntityColumns().forEach(entityColumnDetails -> {

                DDLFieldMetadataHolder fieldMetadataHolder = DDLFieldMetadataHolder.builder()
                        .tableName(tableName)
                        .columnDetails(entityColumnDetails)
                        .columnNameAndDatabaseTypeList(columnNameAndDatabaseTypeList)
                        .foreignNameConstraints(foreignNameConstraints)
                        .entityClass(entityClass)
                        .bibernateEntityMetadata(bibernateEntityMetadata)
                        .build();

                fieldResolvers.stream()
                        .filter(fieldResolver -> fieldResolver.hasFieldToResolve(entityColumnDetails))
                        .findFirst()
                        .ifPresent(fieldResolver -> fieldResolver.handleField(fieldMetadataHolder, ddlMetadata));
            });
            String collectNameDatabaseTypes = String.join(DELIMITER, columnNameAndDatabaseTypeList);
            builder.append(collectNameDatabaseTypes);
            String query = builder.append(BRACKET).toString();
            createTables.add(query);
        });
    }

    private void createIndexQuery(EntityMetadata entityMetadata) {
        String tableName = entityMetadata.getTableName();
        List<IndexMetadata> indexMetadatas = entityMetadata.getIndexMetadatas();
        Map<String, List<String>> indexMetadataMap = new HashMap<>();
        indexMetadatas.forEach(indexMetadata -> {
            String indexName = Optional.ofNullable(indexMetadata.getName())
                    .filter(name -> !name.isEmpty())
                    .orElseGet(DDLUtils::getIndexName);
            String columnList = getSnakeString(indexMetadata.getColumnList());

            checkIfColumnForIndexExists(entityMetadata, columnList);

            indexMetadataMap.computeIfAbsent(indexName, k -> new ArrayList<>()).add(columnList);
        });

        indexMetadataMap.forEach((idxName, list) -> {
            String columns = String.join(DELIMITER, list);
            String createIndexQuery = CREATE_INDEX.formatted(idxName, tableName, columns);
            ddlMetadata.computeIfAbsent(OperationOrder.CREATE_INDEX, k -> new ArrayList<>())
                    .add(createIndexQuery);
        });
    }

    private void checkIfColumnForIndexExists(EntityMetadata entityMetadata, String columnList) {
        boolean hasSuchColumn = entityMetadata.getEntityColumns().stream()
                .anyMatch(entityColumnDetails -> entityColumnDetails
                        .getColumn()
                        .getName()
                        .equals(columnList));
        if (!hasSuchColumn) {
            throw new MappingException("Error generating index for "
                    + "table '%s' [column '%s' does not exist]"
                    .formatted(entityMetadata.getTableName(), columnList));
        }
    }
}
