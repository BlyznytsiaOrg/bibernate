package io.github.blyznytsiaorg.bibernate.ddl.field;

import static io.github.blyznytsiaorg.bibernate.utils.TypeConverter.checkIdTypeForGeneration;
import static io.github.blyznytsiaorg.bibernate.utils.TypeConverter.getPostgresIdTypeForGeneration;
import static io.github.blyznytsiaorg.bibernate.utils.TypeConverter.isInternalJavaTypeSuitableForCreation;

import io.github.blyznytsiaorg.bibernate.ddl.DDLFieldMetadataHolder;
import io.github.blyznytsiaorg.bibernate.ddl.OperationOrder;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityColumnDetails;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.ColumnMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.GeneratedValueMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.SequenceGeneratorMetadata;
import io.github.blyznytsiaorg.bibernate.exception.MappingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoRelationFieldResolver implements FieldResolver {
    public static final String PRIMARY_KEY_FIELD_CREATION = "%s %s primary key";
    public static final String IDENTITY = "IDENTITY";
    public static final String SEQUENCE = "SEQUENCE";
    public static final String DEFAULT_SEQ_TEMPLATE = "%s_%s_seq";  //tableName_columnName_seq
    public static final String SEQUENCE_CREATION = "create sequence %s start with %s increment by %s";
    public static final String SEQUENCE_DROP = "drop sequence if exists %s";
    private static final int DEFAULT_INITIAL_VALUE = 1;
    private static final int DEFAULT_ALLOCATION_SIZE = 1;
    public static final String NAME_DATA_PATTERN_WITH_TIME_ZONE = "%s %s with time zone";
    public static final String TIMESTAMP_DEFAULT_NOW = "%s %s default now()";
    public static final String TIMESTAMP_WITH_TIME_ZONE_DEFAULT_NOW = "%s %s with time zone default now()";
    public static final String NAME_DATA_PATTERN = "%s %s";
    public static final String NOT_NULL = " not null";
    public static final String UNIQUE = " unique";
    @Override
    public boolean hasFieldToResolve(EntityColumnDetails entityColumnDetails) {
        return entityColumnDetails.getManyToMany() == null && entityColumnDetails.getOneToOne() == null
                && entityColumnDetails.getManyToOne() == null && entityColumnDetails.getOneToMany() == null;
    }

    @Override
    public void handleField(DDLFieldMetadataHolder metadataHolder, Map<Integer, List<String>> ddlMetadata) {
        EntityColumnDetails columnDetails = metadataHolder.getColumnDetails();
        String tableName = metadataHolder.getTableName();
        List<String> columnNameAndDatabaseTypeList = metadataHolder.getColumnNameAndDatabaseTypeList();
        isInternalJavaTypeSuitableForCreation(columnDetails, tableName);
        String nameDatabaseType;
        if (columnDetails.getId() != null) {
            nameDatabaseType = processIdField(tableName, columnDetails, ddlMetadata);
        } else if (columnDetails.getColumn().isTimestamp()) {
            nameDatabaseType = processTimestamp(columnDetails);
        } else {
            nameDatabaseType = getColumnNameDatabaseType(columnDetails);
        }
        columnNameAndDatabaseTypeList.add(nameDatabaseType);
    }

    private String processIdField(String tableName, EntityColumnDetails entityColumn,
                                  Map<Integer, List<String>> ddlMetadata) {
        Class<?> fieldType = entityColumn.getFieldType();
        String columnName = entityColumn.getColumn().getName();
        String databaseType = entityColumn.getColumn().getDatabaseType();
        String nameDatabaseType;
        if (entityColumn.getGeneratedValue() == null) {
            nameDatabaseType = PRIMARY_KEY_FIELD_CREATION.formatted(columnName, databaseType);
        } else {
            GeneratedValueMetadata generatedValue = entityColumn.getGeneratedValue();
            if (generatedValue.getStrategy().equals(IDENTITY)) {
                String postgresIdType = getPostgresIdTypeForGeneration(fieldType, tableName);
                nameDatabaseType = PRIMARY_KEY_FIELD_CREATION.formatted(columnName, postgresIdType);
            } else if (generatedValue.getStrategy().equals(SEQUENCE)) {
                checkIdTypeForGeneration(entityColumn.getFieldType(), tableName);
                SequenceGeneratorMetadata sequenceGenerator = entityColumn.getSequenceGenerator();
                generateSequence(generatedValue, sequenceGenerator, tableName, columnName, ddlMetadata);
                nameDatabaseType = PRIMARY_KEY_FIELD_CREATION.formatted(columnName, databaseType);
            } else {
                nameDatabaseType = PRIMARY_KEY_FIELD_CREATION.formatted(columnName, databaseType);
            }
        }
        return nameDatabaseType;
    }

    private void generateSequence(GeneratedValueMetadata generatedValue,
                                  SequenceGeneratorMetadata sequenceGenerator,
                                  String tableName, String columnName, Map<Integer, List<String>> ddlMetadata) {
        String sequenceNameDefault = DEFAULT_SEQ_TEMPLATE.formatted(tableName, columnName);
        if (sequenceGenerator == null || generatedValue.getGenerator() == null
                || generatedValue.getGenerator().isEmpty()) {
            checkForDuplicateNameSequence(ddlMetadata, sequenceNameDefault);

            ddlMetadata.computeIfAbsent(OperationOrder.DROP_SEQUENCE,
                    k -> new ArrayList<>()).add(SEQUENCE_DROP.formatted(sequenceNameDefault));

            ddlMetadata.computeIfAbsent(OperationOrder.CREATE_SEQUENCE,
                    k -> new ArrayList<>()).add(SEQUENCE_CREATION.formatted(sequenceNameDefault,
                    DEFAULT_INITIAL_VALUE, DEFAULT_ALLOCATION_SIZE));
        } else {
            String generatorName = generatedValue.getGenerator();
            String sequenceGeneratorName = sequenceGenerator.getName();
            if (!generatorName.equals(sequenceGeneratorName)) {
                ddlMetadata.computeIfAbsent(OperationOrder.DROP_SEQUENCE,
                        k -> new ArrayList<>()).add(SEQUENCE_DROP.formatted(sequenceNameDefault));

                ddlMetadata.computeIfAbsent(OperationOrder.CREATE_SEQUENCE,
                        k -> new ArrayList<>()).add(SEQUENCE_CREATION.formatted(sequenceNameDefault,
                        DEFAULT_INITIAL_VALUE, DEFAULT_ALLOCATION_SIZE));;
            } else {
                String sequenceName = sequenceGenerator.getSequenceName();
                int initialValue = sequenceGenerator.getInitialValue();
                int allocationSize = sequenceGenerator.getAllocationSize();

                ddlMetadata.computeIfAbsent(OperationOrder.DROP_SEQUENCE,
                        k -> new ArrayList<>()).add(SEQUENCE_DROP.formatted(sequenceName));

                ddlMetadata.computeIfAbsent(OperationOrder.CREATE_SEQUENCE,
                        k -> new ArrayList<>()).add(SEQUENCE_CREATION.formatted(sequenceName,
                        initialValue, allocationSize));
            }
        }
    }

    private void checkForDuplicateNameSequence(Map<Integer, List<String>> ddlMetadata,
                                  String sequenceNameDefault) {
        if (ddlMetadata.get(OperationOrder.CREATE_SEQUENCE).contains(sequenceNameDefault)) {
            throw new MappingException("Duplicate naming for sequence with name '%s'"
                    .formatted(sequenceNameDefault));
        }
    }

    private String processTimestamp(EntityColumnDetails entityColumn) {
        String name = entityColumn.getColumn().getName();
        String databaseType = entityColumn.getColumn().getDatabaseType();
        String nameData;
        if (entityColumn.getColumn().isTimeZone()) {
            if (isCreationTimestamp(entityColumn)) {
                nameData = TIMESTAMP_WITH_TIME_ZONE_DEFAULT_NOW.formatted(name, databaseType);
            } else {
                nameData = NAME_DATA_PATTERN_WITH_TIME_ZONE.formatted(name, databaseType);
            }
        } else {
            if (isCreationTimestamp(entityColumn)) {
                nameData = TIMESTAMP_DEFAULT_NOW.formatted(name, databaseType);
            } else {
                nameData = NAME_DATA_PATTERN.formatted(name, databaseType);
            }
        }
        return nameData;
    }

    private boolean isCreationTimestamp(EntityColumnDetails entityColumn) {
        return entityColumn.getCreationTimestampMetadata() != null;
    }

    private String getColumnNameDatabaseType(EntityColumnDetails entityColumn) {
        ColumnMetadata column = entityColumn.getColumn();
        String columnName = column.getName();
        String databaseType = column.getDatabaseType();
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
}
