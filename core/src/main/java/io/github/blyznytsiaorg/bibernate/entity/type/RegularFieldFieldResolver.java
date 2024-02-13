package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.entity.EntityPersistent;
import io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.columnName;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getValueFromResultSet;

/**
 * Implementation of {@link TypeFieldResolver} for resolving custom injection values for regular fields of an entity class.
 * The regular fields are the simple fields that are not Entity or Collections
 * <p>
 * This resolver is responsible for determining if a given field of an entity class is a regular field
 * and preparing the value to be injected into the field.
 */
public class RegularFieldFieldResolver implements TypeFieldResolver {

    /**
     * <p>
     * Determines if the resolver is appropriate for the given field.
     * This implementation returns {@code true} if the field is a regular field (i.e., not an association).
     *
     * @param field the field to be evaluated for custom injection
     * @return {@code true} if the field is a regular field, {@code false} otherwise
     */
    @Override
    public boolean isAppropriate(Field field) {
        return EntityRelationsUtils.isRegularField(field);
    }

    /**
     * <p>
     * Prepares the value to be injected into the specified regular field of the entity.
     * This implementation extracts the value from the result set using the column name corresponding to the field.
     *
     * @param field            the regular field to receive the injected value
     * @param resultSet        the result set containing the data from which the value will be extracted
     * @param entityClass      the entity object to which the field belongs
     * @param entityPersistent the persistent metadata associated with the entity
     * @return the value to be injected into the regular field
     */
    @Override
    public Object prepareValueForFieldInjection(Field field,
                                                ResultSet resultSet,
                                                Object entityClass,
                                                EntityPersistent entityPersistent) {
        var fieldName = columnName(field);
        return getValueFromResultSet(field, resultSet, fieldName);
    }
}
