package io.github.blyznytsiaorg.bibernate.ddl.field;

import io.github.blyznytsiaorg.bibernate.ddl.DDLFieldMetadataHolder;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityColumnDetails;
import io.github.blyznytsiaorg.bibernate.exception.MappingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The FieldResolver interface defines methods for resolving fields during DDL query creation.
 * <p>
 * Implementing classes must provide functionality to check if a field needs resolution and to
 * handle the resolution process.
 *
 * @see DDLFieldMetadataHolder
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public interface FieldResolver {
     String NAME_DATA_PATTERN = "%s %s";
     String CREATE_CONSTRAINT = "alter table if exists %s add constraint %s foreign key(%s) references %s";
     String DROP_CONSTRAINT = "alter table if exists %s drop constraint if exists %s";

    /**
     * Determines whether the given entity column requires field resolution.
     *
     * @param entityColumnDetails the details of the entity column
     * @return true if the field needs resolution, otherwise false
     */
    boolean hasFieldToResolve(EntityColumnDetails entityColumnDetails);

    /**
     * Handles the resolution of a field, generating and adding DDL queries to the metadata holder.
     *
     * @param metadataHolder the holder containing metadata for DDL query generation
     * @param ddlMetadata     the metadata for DDL queries
     */
    void handleField(DDLFieldMetadataHolder metadataHolder, Map<Integer, List<String>> ddlMetadata);

    /**
     * Checks if a foreign key name already exists in the set of foreign key constraints.
     * If a duplicate name is found it throws a MappingException.
     *
     * @param foreignKeyName         the name of the foreign key constraint to check
     * @param foreignNameConstraints the set of existing foreign key constraints
     * @throws MappingException if a duplicate foreign key name is encountered
     */
    default void checkForeignKeyName(String foreignKeyName,
                                     Set<String> foreignNameConstraints) {
        if (!foreignNameConstraints.add(foreignKeyName)) {
            throw new MappingException("Duplicate in foreign key name '%s'".formatted(foreignKeyName));
        }
    }
}
