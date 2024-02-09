package io.github.blyznytsiaorg.bibernate.ddl;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getSnakeString;
import static io.github.blyznytsiaorg.bibernate.utils.TypeConverter.getPostgresIdType;

import io.github.blyznytsiaorg.bibernate.annotation.ManyToMany;
import io.github.blyznytsiaorg.bibernate.annotation.ManyToOne;
import io.github.blyznytsiaorg.bibernate.annotation.OneToOne;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityColumnDetails;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.ColumnMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.GeneratedValueMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.IndexMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.JoinColumnMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.JoinTableMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.ManyToManyMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.SequenceGeneratorMetadata;
import io.github.blyznytsiaorg.bibernate.exception.MappingException;
import io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder;
import io.github.blyznytsiaorg.bibernate.utils.DDLUtils;
import io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils;
import lombok.Getter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Getter
public class DDLQueryCreator {
    public static final String DEFAULT_SEQ_TEMPLATE = "%s_%s_seq";  //tableName_columnName_seq
    public static final String SEQUENCE_CREATION = "create sequence %s start with %s increment by %s";
    public static final String SEQUENCE_DROP = "drop sequence if exists %s";
    private static final int DEFAULT_INITIAL_VALUE = 1;
    private static final int DEFAULT_ALLOCATION_SIZE = 1;
    public static final String PRIMARY_KEY_FIELD_CREATION = "%s %s primary key";
    public static final String DROP_TABLE = "drop table if exists %s cascade";
    public static final String CREATE_CONSTRAINT = "alter table if exists %s add constraint %s foreign key(%s) references %s";
    public static final String DROP_CONSTRAINT = "alter table if exists %s drop constraint if exists %s";
    public static final String CREATE_TABLE_MANY_TO_MANY = "create table %s (%s %s, %s %s, primary key (%s, %s))";
    public static final String CREATE_INDEX = "create index %s on %s (%s)";
    public static final String NAME_DATA_PATTERN = "%s %s";
    public static final String NOT_NULL = " not null";
    public static final String UNIQUE = " unique";
    public static final String IDENTITY = "IDENTITY";
    public static final String SEQUENCE = "SEQUENCE";
    public static final String DELIMITER = ", ";
    public static final String JAVA_LANG = "java.lang";
    public static final String JAVA_MATH = "java.math";
    public static final String JAVA_SQL = "java.sql";
    public static final String JAVA_TIME = "java.time";
    public static final String CREATE_TABLE = "create table %s (";
    public static final String BRACKET = ")";
    private final List<String> dropSequences = new ArrayList<>();
    private final List<String> createSequences = new ArrayList<>();
    private final List<String> createTables = new ArrayList<>();
    private final List<String> createIndex = new ArrayList<>();
    private final List<String> createConstraints = new ArrayList<>();
    private final List<String> dropConstraints = new ArrayList<>();
    private final List<String> dropTables = new ArrayList<>();
    private final Map<Class<?>, EntityMetadata> bibernateEntityMetadata;

    public DDLQueryCreator() {
        bibernateEntityMetadata = BibernateContextHolder.getBibernateEntityMetadata();
        createQueries();
    }

    public void createQueries() {

        bibernateEntityMetadata.forEach((entityClass, entityMetadata) -> {

            Set<String> foreignNameConstraints = new HashSet<>();

            createIndexQuery(entityMetadata);

            String tableName = entityMetadata.getTableName();

            dropTables.add(DROP_TABLE.formatted(tableName));

            StringBuilder builder = new StringBuilder(CREATE_TABLE.formatted(tableName));

            List<String> columnNameAndDatabaseTypeList = new ArrayList<>();

            entityMetadata.getEntityColumns().forEach(entityColumn -> {

                Class<?> fieldType = entityColumn.getFieldType();
                Field field = entityColumn.getField();

                if (isInternalJavaType(fieldType)) {

                    processInternalJavaType(tableName, columnNameAndDatabaseTypeList,
                            entityColumn, fieldType);

                } else if (field.isAnnotationPresent(ManyToMany.class)) {

                    processToManyRelations(foreignNameConstraints, tableName, entityColumn);
                } else if (ifIsToOneRelation(field)) {

                    processToOneRelations(entityClass, foreignNameConstraints, tableName,
                            columnNameAndDatabaseTypeList, entityColumn, fieldType);
                }
            });
            String collectNameDatabaseTypes = String.join(DELIMITER, columnNameAndDatabaseTypeList);
            builder.append(collectNameDatabaseTypes);
            String query = builder.append(BRACKET).toString();
            createTables.add(query);
        });
    }

    private static boolean ifIsToOneRelation(Field field) {
        if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class)) {
            OneToOne oneToOne = field.getAnnotation(OneToOne.class);
            return oneToOne == null || oneToOne.mappedBy().isEmpty();
        }
        return false;
    }

    private void processInternalJavaType(String tableName,
                                         List<String> columnNameAndDatabaseTypeList,
                                         EntityColumnDetails entityColumn, Class<?> fieldType) {
        ColumnMetadata column = entityColumn.getColumn();
        String columnName = column.getName();
        String databaseType = column.getDatabaseType();

        String nameDatabaseType;
        if (entityColumn.getId() != null) {
            if (entityColumn.getGeneratedValue() == null) {
                nameDatabaseType = PRIMARY_KEY_FIELD_CREATION.formatted(columnName, databaseType);
            } else {
                GeneratedValueMetadata generatedValue = entityColumn.getGeneratedValue();
                if (generatedValue.getStrategy().equals(IDENTITY)) {
                    String postgresIdType = getPostgresIdType(fieldType, tableName);
                    nameDatabaseType = PRIMARY_KEY_FIELD_CREATION.formatted(columnName, postgresIdType);
                } else if (generatedValue.getStrategy().equals(SEQUENCE)) {
                    checkTypeForSequence(tableName, entityColumn.getFieldType());
                    SequenceGeneratorMetadata sequenceGenerator = entityColumn.getSequenceGenerator();
                    generateSequence(generatedValue, sequenceGenerator, tableName, columnName);
                    nameDatabaseType = PRIMARY_KEY_FIELD_CREATION.formatted(columnName, databaseType);
                } else {
                    nameDatabaseType = PRIMARY_KEY_FIELD_CREATION.formatted(columnName, databaseType);
                }
            }
        } else {
            nameDatabaseType = getColumnNameDatabaseType(column, columnName, databaseType);
        }
        columnNameAndDatabaseTypeList.add(nameDatabaseType);
    }

    private void processToManyRelations(Set<String> foreignNameConstraints,
                                        String tableName, EntityColumnDetails entityColumn) {
        ManyToManyMetadata manyToMany = entityColumn.getManyToMany();
        if (manyToMany.getMappedBy().isEmpty()) {
            JoinTableMetadata joinTable = entityColumn.getJoinTable();
            String joinTableName = joinTable.getName();
            String joinColumnName = joinTable.getJoinColumn();
            String inverseJoinColumnName = joinTable.getInverseJoinColumn();
            String databaseTypeJoinColumn = joinTable.getJoinColumnDatabaseType();
            String databaseTypeInverseJoinColumn = joinTable.getInverseJoinColumnDatabaseType();
            String foreignKey = joinTable.getForeignKey();
            String inverseForeignKey = joinTable.getInverseForeignKey();

            Class<?> collectionGenericType = EntityReflectionUtils.getCollectionGenericType(entityColumn.getField());
            EntityMetadata relationEntityMetadata = bibernateEntityMetadata.get(collectionGenericType);


            var query = CREATE_TABLE_MANY_TO_MANY
                    .formatted(joinTableName, joinColumnName, databaseTypeJoinColumn,
                            inverseJoinColumnName, databaseTypeInverseJoinColumn,
                            joinColumnName, inverseJoinColumnName);

            createTables.add(query);

            processForeignKeyConstraintJoinColumn(foreignNameConstraints, tableName,
                    foreignKey, joinTableName, joinColumnName);

            processForeignKeyConstraintInverseJoinColumn(inverseForeignKey,
                    foreignNameConstraints, joinTableName, relationEntityMetadata,
                    inverseJoinColumnName);
        }
    }

    private void processToOneRelations(Class<?> entityClass, Set<String> foreignNameConstraints,
                                       String tableName, List<String> nameDatabaseTypes,
                                       EntityColumnDetails entityColumn, Class<?> fieldType) {
        EntityMetadata metadataOfRelation = bibernateEntityMetadata.get(fieldType);
        checkIfRelationExists(entityClass, fieldType, metadataOfRelation);
        JoinColumnMetadata joinColumn = entityColumn.getJoinColumn();
        String joinColumnName = joinColumn.getName();
        String databaseType = joinColumn.getDatabaseType();
        String nameDatabaseType = NAME_DATA_PATTERN.formatted(joinColumnName, databaseType);
        nameDatabaseTypes.add(nameDatabaseType);

        processForeignKeyConstraint(joinColumn.getForeignKeyName(),
                foreignNameConstraints, tableName, metadataOfRelation,
                joinColumnName);
    }

    private static void checkIfRelationExists(Class<?> entityClass, Class<?> fieldType,
                                              EntityMetadata metadataOfRelation) {
        if (metadataOfRelation == null) {
            throw new MappingException("Can't find relation '%s' for the class '%s'"
                    .formatted(fieldType.getSimpleName(), entityClass.getSimpleName()));
        }
    }

    private void processForeignKeyConstraint(String foreignKey, Set<String> foreignNameConstraints,
                                             String tableName, EntityMetadata metadataOfRelation,
                                             String joinColumnName) {

        checkForeignKeyName(foreignKey, foreignNameConstraints);

        String dropRelationConstraint = DROP_CONSTRAINT.formatted(tableName, foreignKey);
        dropConstraints.add(dropRelationConstraint);

        String relationTableName = metadataOfRelation.getTableName();
        String createRelationConstraint = CREATE_CONSTRAINT.formatted(tableName,
                foreignKey, joinColumnName, relationTableName);
        createConstraints.add(createRelationConstraint);
    }

    private void processForeignKeyConstraintInverseJoinColumn(String foreignKey,
                                                              Set<String> foreignNameConstraints,
                                                              String joinTableName,
                                                              EntityMetadata relationEntityMetadata,
                                                              String inverseJoinColumnName) {
        checkForeignKeyName(foreignKey, foreignNameConstraints);
        String inverseJoinColumnDropConstraint = DROP_CONSTRAINT.formatted(joinTableName,
                foreignKey);
        dropConstraints.add(inverseJoinColumnDropConstraint);

        String inverseJoinTableName = relationEntityMetadata.getTableName();
        String inverseJoinColumnCreateConstraint = CREATE_CONSTRAINT.formatted(joinTableName,
                foreignKey, inverseJoinColumnName, inverseJoinTableName);
        createConstraints.add(inverseJoinColumnCreateConstraint);
    }

    private void processForeignKeyConstraintJoinColumn(Set<String> foreignNameConstraints,
                                                       String tableName,
                                                       String foreignKey,
                                                       String joinTableName,
                                                       String joinColumnName) {
        checkForeignKeyName(foreignKey, foreignNameConstraints);

        String joinColumnDropConstraint = DROP_CONSTRAINT.formatted(joinTableName, foreignKey);
        dropConstraints.add(joinColumnDropConstraint);

        String joinColumnCreateConstraint = CREATE_CONSTRAINT.formatted(joinTableName, foreignKey,
                joinColumnName, tableName);
        createConstraints.add(joinColumnCreateConstraint);
    }

    private String getColumnNameDatabaseType(ColumnMetadata column, String columnName,
                                             String databaseType) {
        StringBuilder nameDatabaseType = new StringBuilder(NAME_DATA_PATTERN.formatted(columnName, databaseType));

        boolean isUnique = column.isUnique();
        boolean isNullable = column.isNullable();
        if (!isNullable) {
            nameDatabaseType.append(NOT_NULL);
        }
        if (isUnique) {
            nameDatabaseType.append(UNIQUE);
        }
        return nameDatabaseType.toString();
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
            createIndex.add(createIndexQuery);
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

    private void checkTypeForSequence(String tableName, Class<?> type) {
        if (!type.equals(Long.class) && !type.equals(long.class)
                && !type.equals(Integer.class) && !type.equals(int.class)) {
            throw new MappingException("Error creating SQL commands for "
                    + "table '%s' [illegal identity column type '%s'] for sequence"
                    .formatted(tableName, type.getSimpleName()));
        }
    }

    private void checkForeignKeyName(String foreignKeyName,
                                     Set<String> foreignNameConstraints) {
        if (!foreignNameConstraints.add(foreignKeyName)) {
            throw new MappingException("Duplicate in foreign key name '%s'".formatted(foreignNameConstraints));
        }
    }

    private void generateSequence(GeneratedValueMetadata generatedValue,
                                  SequenceGeneratorMetadata sequenceGenerator,
                                  String tableName, String columnName) {
        String sequenceNameDefault = DEFAULT_SEQ_TEMPLATE.formatted(tableName, columnName);
        if (sequenceGenerator == null || generatedValue.getGenerator() == null
                || generatedValue.getGenerator().isEmpty()) {
            if (createSequences.contains(sequenceNameDefault)) {
                throw new MappingException("Duplicate naming for sequence with name '%s'"
                        .formatted(sequenceNameDefault));
            }
            dropSequences.add(SEQUENCE_DROP.formatted(sequenceNameDefault));
            createSequences.add(SEQUENCE_CREATION.formatted(sequenceNameDefault, DEFAULT_INITIAL_VALUE,
                    DEFAULT_ALLOCATION_SIZE));
        } else {
            String generatorName = generatedValue.getGenerator();
            String sequenceGeneratorName = sequenceGenerator.getName();
            if (!generatorName.equals(sequenceGeneratorName)) {
                dropSequences.add(SEQUENCE_DROP.formatted(sequenceNameDefault));
                createSequences.add(SEQUENCE_CREATION.formatted(sequenceNameDefault, DEFAULT_INITIAL_VALUE,
                        DEFAULT_ALLOCATION_SIZE));
            } else {
                String sequenceName = sequenceGenerator.getSequenceName();
                int initialValue = sequenceGenerator.getInitialValue();
                int allocationSize = sequenceGenerator.getAllocationSize();
                dropSequences.add(SEQUENCE_DROP.formatted(sequenceName));
                createSequences.add(SEQUENCE_CREATION.formatted(sequenceName, initialValue, allocationSize));
            }
        }
    }

    private boolean isInternalJavaType(Class<?> fieldType) {
        String packageName = fieldType.getPackageName();
        return packageName.equals(JAVA_LANG) || packageName.equals(JAVA_MATH)
                || packageName.equals(JAVA_SQL) || packageName.equals(JAVA_TIME);
    }
}
