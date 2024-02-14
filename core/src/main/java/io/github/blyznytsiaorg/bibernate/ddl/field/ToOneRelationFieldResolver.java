package io.github.blyznytsiaorg.bibernate.ddl.field;


import io.github.blyznytsiaorg.bibernate.annotation.ManyToOne;
import io.github.blyznytsiaorg.bibernate.annotation.OneToOne;
import io.github.blyznytsiaorg.bibernate.ddl.DDLFieldMetadataHolder;
import io.github.blyznytsiaorg.bibernate.ddl.OperationOrder;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityColumnDetails;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadata;
import io.github.blyznytsiaorg.bibernate.entity.metadata.model.JoinColumnMetadata;
import io.github.blyznytsiaorg.bibernate.exception.MappingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves fields representing one-to-one or many-to-one relationships in entity columns.
 *
 * @see FieldResolver
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class ToOneRelationFieldResolver implements FieldResolver {

    /**
     * Determines if there is a field to resolve based on the provided entity column details.
     *
     * @param entityColumnDetails The entity column details to check.
     * @return True if there is a field to resolve, false otherwise.
     */
    @Override
    public boolean hasFieldToResolve(EntityColumnDetails entityColumnDetails) {
        Field field = entityColumnDetails.getField();
        if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class)) {
            OneToOne oneToOne = field.getAnnotation(OneToOne.class);
            return oneToOne == null || oneToOne.mappedBy().isEmpty();
        }
        return false;
    }

    /**
     * Handles the resolution of the given entity column details by generating database type information
     * and adding it to the list of column name and database type.
     *
     * @param metadataHolder The holder containing metadata related to the field.
     * @param ddlMetadata    The map storing DDL metadata.
     */
    @Override
    public void handleField(DDLFieldMetadataHolder metadataHolder, Map<Integer, List<String>> ddlMetadata) {
        EntityColumnDetails entityColumn = metadataHolder.getColumnDetails();
        Map<Class<?>, EntityMetadata> bibernateEntityMetadata = metadataHolder.getBibernateEntityMetadata();
        Class<?> entityClass = metadataHolder.getEntityClass();
        List<String> columnNameAndDatabaseTypeList = metadataHolder.getColumnNameAndDatabaseTypeList();
        String tableName = metadataHolder.getTableName();
        Set<String> foreignNameConstraints = metadataHolder.getForeignNameConstraints();

        Class<?> fieldType = entityColumn.getFieldType();
        EntityMetadata metadataOfRelation = bibernateEntityMetadata.get(fieldType);
        checkIfRelationExists(entityClass, fieldType, metadataOfRelation);
        JoinColumnMetadata joinColumn = entityColumn.getJoinColumn();
        String joinColumnName = joinColumn.getName();
        String databaseType = joinColumn.getDatabaseType();
        String nameDatabaseType = NAME_DATA_PATTERN.formatted(joinColumnName, databaseType);

        columnNameAndDatabaseTypeList.add(nameDatabaseType);

        processForeignKeyConstraint(joinColumn.getForeignKeyName(),
                foreignNameConstraints, tableName, metadataOfRelation,
                joinColumnName, ddlMetadata);
    }

    private void processForeignKeyConstraint(String foreignKey, Set<String> foreignNameConstraints,
                                             String tableName, EntityMetadata metadataOfRelation,
                                             String joinColumnName, Map<Integer, List<String>> ddlMetadata) {

        checkForeignKeyName(foreignKey, foreignNameConstraints);

        String dropRelationConstraint = DROP_CONSTRAINT.formatted(tableName, foreignKey);
        ddlMetadata.computeIfAbsent(OperationOrder.DROP_CONSTRAINT, k -> new ArrayList<>()).add(dropRelationConstraint);

        String relationTableName = metadataOfRelation.getTableName();
        String createRelationConstraint = CREATE_CONSTRAINT.formatted(tableName,
                foreignKey, joinColumnName, relationTableName);
        ddlMetadata.computeIfAbsent(OperationOrder.CREATE_CONSTRAINT, k -> new ArrayList<>()).add(createRelationConstraint);
    }

    private void checkIfRelationExists(Class<?> entityClass, Class<?> fieldType,
                                       EntityMetadata metadataOfRelation) {
        if (metadataOfRelation == null) {
            throw new MappingException("Can't find relation '%s' for the class '%s'"
                    .formatted(fieldType.getSimpleName(), entityClass.getSimpleName()));
        }
    }
}
