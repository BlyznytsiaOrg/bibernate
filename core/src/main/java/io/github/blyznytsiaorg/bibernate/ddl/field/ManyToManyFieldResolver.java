package io.github.blyznytsiaorg.bibernate.ddl.field;

import io.github.blyznytsiaorg.bibernate.annotation.ManyToMany;
import io.github.blyznytsiaorg.bibernate.ddl.DDLFieldMetadataHolder;
import io.github.blyznytsiaorg.bibernate.ddl.OperationOrder;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityColumnDetails;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.JoinTableMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.ManyToManyMetadata;
import io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ManyToManyFieldResolver implements FieldResolver {
    public static final String CREATE_TABLE_MANY_TO_MANY = "create table %s (%s %s, %s %s, primary key (%s, %s))";

    @Override
    public boolean hasFieldToResolve(EntityColumnDetails entityColumnDetails) {
        Field field = entityColumnDetails.getField();
        return field.isAnnotationPresent(ManyToMany.class);
    }

    @Override
    public void handleField(DDLFieldMetadataHolder metadataHolder, Map<Integer, List<String>> ddlMetadata) {
        EntityColumnDetails entityColumnDetails = metadataHolder.getColumnDetails();
        Map<Class<?>, EntityMetadata> bibernateEntityMetadata = metadataHolder.getBibernateEntityMetadata();
        Set<String> foreignNameConstraints = metadataHolder.getForeignNameConstraints();
        String tableName = metadataHolder.getTableName();

        ManyToManyMetadata manyToMany = entityColumnDetails.getManyToMany();
        if (manyToMany.getMappedBy().isEmpty()) {
            JoinTableMetadata joinTable = entityColumnDetails.getJoinTable();
            String joinTableName = joinTable.getName();
            String joinColumnName = joinTable.getJoinColumn();
            String inverseJoinColumnName = joinTable.getInverseJoinColumn();
            String databaseTypeJoinColumn = joinTable.getJoinColumnDatabaseType();
            String databaseTypeInverseJoinColumn = joinTable.getInverseJoinColumnDatabaseType();
            String foreignKey = joinTable.getForeignKey();
            String inverseForeignKey = joinTable.getInverseForeignKey();

            Class<?> collectionGenericType = EntityReflectionUtils.getCollectionGenericType(entityColumnDetails.getField());
            EntityMetadata relationEntityMetadata = bibernateEntityMetadata.get(collectionGenericType);


            var query = CREATE_TABLE_MANY_TO_MANY
                    .formatted(joinTableName, joinColumnName, databaseTypeJoinColumn,
                            inverseJoinColumnName, databaseTypeInverseJoinColumn,
                            joinColumnName, inverseJoinColumnName);

            ddlMetadata.computeIfAbsent(OperationOrder.CREATE_TABLE, k -> new ArrayList<>()).add(query);

            processForeignKeyConstraintJoinColumn(foreignNameConstraints, tableName,
                    foreignKey, joinTableName, joinColumnName, ddlMetadata);

            processForeignKeyConstraintInverseJoinColumn(inverseForeignKey,
                    foreignNameConstraints, joinTableName, relationEntityMetadata,
                    inverseJoinColumnName, ddlMetadata);
        }
    }

    private void processForeignKeyConstraintJoinColumn(Set<String> foreignNameConstraints,
                                                       String tableName,
                                                       String foreignKey,
                                                       String joinTableName,
                                                       String joinColumnName,
                                                       Map<Integer, List<String>> ddlMetadata) {
        checkForeignKeyName(foreignKey, foreignNameConstraints);

        String joinColumnDropConstraint = DROP_CONSTRAINT.formatted(joinTableName, foreignKey);
        ddlMetadata.computeIfAbsent(OperationOrder.DROP_CONSTRAINT, k -> new ArrayList<>())
                .add(joinColumnDropConstraint);

        String joinColumnCreateConstraint = CREATE_CONSTRAINT.formatted(joinTableName, foreignKey,
                joinColumnName, tableName);
        ddlMetadata.computeIfAbsent(OperationOrder.CREATE_CONSTRAINT, k -> new ArrayList<>())
                .add(joinColumnCreateConstraint);
    }

    private void processForeignKeyConstraintInverseJoinColumn(String foreignKey,
                                                              Set<String> foreignNameConstraints,
                                                              String joinTableName,
                                                              EntityMetadata relationEntityMetadata,
                                                              String inverseJoinColumnName,
                                                              Map<Integer, List<String>> ddlMetadata) {
        checkForeignKeyName(foreignKey, foreignNameConstraints);
        String inverseJoinColumnDropConstraint = DROP_CONSTRAINT.formatted(joinTableName,
                foreignKey);
        ddlMetadata.computeIfAbsent(OperationOrder.DROP_CONSTRAINT, k -> new ArrayList<>())
                .add(inverseJoinColumnDropConstraint);

        String inverseJoinTableName = relationEntityMetadata.getTableName();
        String inverseJoinColumnCreateConstraint = CREATE_CONSTRAINT.formatted(joinTableName,
                foreignKey, inverseJoinColumnName, inverseJoinTableName);
        ddlMetadata.computeIfAbsent(OperationOrder.CREATE_CONSTRAINT, k -> new ArrayList<>())
                .add(inverseJoinColumnCreateConstraint);
    }
}
