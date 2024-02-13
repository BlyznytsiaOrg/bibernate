package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.entity.EntityPersistent;
import io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import static io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder.getBibernateSession;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getValueFromResultSetByColumn;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.joinColumnName;

/**
 * Implementation of {@link TypeFieldResolver} for resolving custom injection values for entity fields of an entity class.
 * <p>
 * This resolver is responsible for determining if a given field of an entity class represents an association with another entity
 * and preparing the value to be injected into the field.
 */
public class EntityFieldResolver implements TypeFieldResolver {

    /**
     * <p>
     * Determines if the resolver is appropriate for the given field.
     * This implementation returns {@code true} if the field represents an association with another entity.
     *
     * @param field the field to be evaluated for custom injection
     * @return {@code true} if the field represents an entity association, {@code false} otherwise
     */
    @Override
    public boolean isAppropriate(Field field) {
        return EntityRelationsUtils.isEntityField(field);
    }

    /**
     * <p>
     * Prepares the value to be injected into the specified field of the entity.
     * This implementation retrieves the associated entity from the database.
     *
     * @param field            the field to receive the injected value
     * @param resultSet        the result set containing the data from which the value will be extracted
     * @param entity           the entity object to which the field belongs
     * @param entityPersistent the persistent metadata associated with the entity
     * @return the associated entity retrieved from the database, or {@code null} if not found
     */
    @Override
    public Object prepareValueForFieldInjection(Field field,
                                                ResultSet resultSet,
                                                Object entity,
                                                EntityPersistent entityPersistent) {
        var session = getBibernateSession();
        var joinColumnName = joinColumnName(field);
        var joinColumnValue = getValueFromResultSetByColumn(resultSet, joinColumnName);

        return session.findById(field.getType(), joinColumnValue)
                .orElse(null);
    }
}
