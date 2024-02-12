package io.github.blyznytsiaorg.bibernate.ddl.field;

import io.github.blyznytsiaorg.bibernate.ddl.DDLFieldMetadataHolder;
import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityColumnDetails;
import io.github.blyznytsiaorg.bibernate.exception.MappingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FieldResolver {
     String NAME_DATA_PATTERN = "%s %s";
     String CREATE_CONSTRAINT = "alter table if exists %s add constraint %s foreign key(%s) references %s";
     String DROP_CONSTRAINT = "alter table if exists %s drop constraint if exists %s";

    boolean hasFieldToResolve(EntityColumnDetails entityColumnDetails);

    void handleField(DDLFieldMetadataHolder metadataHolder, Map<Integer, List<String>> ddlMetadata);

    default void checkForeignKeyName(String foreignKeyName,
                                     Set<String> foreignNameConstraints) {
        if (!foreignNameConstraints.add(foreignKeyName)) {
            throw new MappingException("Duplicate in foreign key name '%s'".formatted(foreignKeyName));
        }
    }
}
