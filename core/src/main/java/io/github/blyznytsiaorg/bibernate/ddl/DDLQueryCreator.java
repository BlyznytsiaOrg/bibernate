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

/**
 * The DDLQueryCreator class is responsible for creating Data Definition Language (DDL) queries
 * based on the metadata of entities in the Bibernate framework. It generates queries for operations
 * such as creating and dropping tables, indexes and sequences. Additionally, it handles the resolution
 * of field mappings and the creation of queries based on entity metadata.
 * <p>
 * This class also provides methods for validating and creating DDL queries for various database operations.
 * <p>
 * The DDLQueryCreator class is designed to work in conjunction with other components of the Bibernate framework
 * to facilitate database schema management and migration tasks.
 *
 * @see io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadataCollector
 * @see EntityMetadata
 * @see DDLFieldMetadataHolder
 * @see OperationOrder
 * @see FieldResolver
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
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

    /**
     * Constructs a new DDLQueryCreator instance.
     * <p>
     * Initializes the DDL query metadata map and field resolvers list.
     * Retrieves Bibernate entity metadata and sets up DDL metadata for various operations such as dropping
     * constraints, tables and sequences, as well as creating sequences, tables, indexes and constraints.
     * Finally, it invokes the createQueries() method to generate the DDL queries based on the entity metadata.
     *
     * @see BibernateContextHolder
     * @see OperationOrder
     */
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

    /**
     * Generates Data Definition Language (DDL) queries based on the metadata of Bibernate entities.
     * <p>
     * Iterates over each Bibernate entity metadata and creates DDL queries for operations such as
     * dropping tables, creating tables and creating indexes. For each entity, it creates DDL queries
     * for its columns and resolves any associated field mappings using field resolvers.
     * <p>
     * The generated DDL queries are added to the respective lists in the DDL metadata map for further processing.
     *
     * @see NoRelationFieldResolver
     * @see ToOneRelationFieldResolver
     * @see ManyToManyFieldResolver
     * @since 1.0
     */
    public void createQueries() {

        bibernateEntityMetadata.forEach((entityClass, entityMetadata) -> {

            var foreignNameConstraints = new HashSet<String>();
            createIndexQuery(entityMetadata);
            var tableName = entityMetadata.getTableName();

            ddlMetadata.computeIfAbsent(OperationOrder.DROP_TABLE, k -> new ArrayList<>())
                    .add(DROP_TABLE.formatted(tableName));

            var builder = new StringBuilder(CREATE_TABLE.formatted(tableName));
            var columnNameAndDatabaseTypeList = new ArrayList<String>();

            entityMetadata.getEntityColumns().forEach(entityColumnDetails -> {

                var fieldMetadataHolder = DDLFieldMetadataHolder.builder()
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
            var collectNameDatabaseTypes = String.join(DELIMITER, columnNameAndDatabaseTypeList);
            builder.append(collectNameDatabaseTypes);
            var query = builder.append(BRACKET).toString();
            createTables.add(query);
        });
    }

    /**
     * Creates Data Definition Language (DDL) queries for creating indexes on the specified entity table.
     *
     * @param entityMetadata the metadata of the entity for which indexes are created
     */
    private void createIndexQuery(EntityMetadata entityMetadata) {
        var tableName = entityMetadata.getTableName();
        var indexMetadatas = entityMetadata.getIndexMetadatas();
        var indexMetadataMap = new HashMap<String, List<String>>();
        indexMetadatas.forEach(indexMetadata -> {
            var indexName = Optional.ofNullable(indexMetadata.getName())
                    .filter(name -> !name.isEmpty())
                    .orElseGet(DDLUtils::getIndexName);
            var columnList = getSnakeString(indexMetadata.getColumnList());

            checkIfColumnForIndexExists(entityMetadata, columnList);

            indexMetadataMap.computeIfAbsent(indexName, k -> new ArrayList<>()).add(columnList);
        });

        indexMetadataMap.forEach((idxName, list) -> {
            var columns = String.join(DELIMITER, list);
            var createIndexQuery = CREATE_INDEX.formatted(idxName, tableName, columns);
            ddlMetadata.computeIfAbsent(OperationOrder.CREATE_INDEX, k -> new ArrayList<>())
                    .add(createIndexQuery);
        });
    }

    /**
     * Checks if the specified column exists in the entity metadata for creating an index.
     * <p>
     * Verifies whether the specified column exists in the entity metadata list of columns.
     * If the column does not exist, it throws a MappingException indicating that the column
     * does not exist for creating an index.
     *
     * @param entityMetadata the metadata of the entity
     * @param columnList     the list of columns for which the index is being created
     * @throws MappingException if the specified column does not exist in the entity metadata
     */
    private void checkIfColumnForIndexExists(EntityMetadata entityMetadata, String columnList) {
        var hasSuchColumn = entityMetadata.getEntityColumns().stream()
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
