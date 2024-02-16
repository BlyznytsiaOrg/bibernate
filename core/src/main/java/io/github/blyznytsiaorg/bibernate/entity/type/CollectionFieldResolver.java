package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.collection.PersistentList;
import io.github.blyznytsiaorg.bibernate.entity.EntityPersistent;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder;
import io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Objects;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getCollectionGenericType;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getEntityId;
import static io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils.*;

/**
 * Implementation of {@link TypeFieldResolver} for resolving custom injection values for collection fields of an entity class.
 * <p>
 * This resolver is responsible for determining if a given field of an entity class represents a collection relationship
 * and preparing the value to be injected into the field.
 */
public class CollectionFieldResolver implements TypeFieldResolver {

    /**
     * <p>
     * Determines if the resolver is appropriate for the given field.
     * This implementation returns {@code true} if the field represents a collection relationship.
     *
     * @param field the field to be evaluated for custom injection
     * @return {@code true} if the field represents a collection relationship, {@code false} otherwise
     */
    @Override
    public boolean isAppropriate(Field field) {
        return EntityRelationsUtils.isCollectionField(field);
    }

    /**
     * <p>
     * Prepares the value to be injected into the specified field of the entity.
     * This implementation retrieves the associated entities from the database based on the collection relationship type.
     *
     * @param field            the field to receive the injected value
     * @param resultSet        the result set containing the data from which the value will be extracted
     * @param entity           the entity object to which the field belongs
     * @param entityPersistent the persistent metadata associated with the entity
     * @return the value to be injected into the field, typically a collection of associated entities
     * @throws BibernateGeneralException if an error occurs while retrieving the associated entities
     */
    @Override
    public Object prepareValueForFieldInjection(Field field,
                                                ResultSet resultSet,
                                                Object entity,
                                                EntityPersistent entityPersistent) {
        var entityId = getEntityId(field, resultSet);

        if (Objects.isNull(entityId)) {
            throw new BibernateGeneralException("Unable to get [%s] from entity [%s] without having the entity id."
                    .formatted(field.getName(), field.getDeclaringClass()));
        }
        
        var collectionGenericType = getCollectionGenericType(field);
        var session = BibernateContextHolder.getBibernateSession();

        if (isOneToMany(field)) {
            return new PersistentList<>(() ->
                    session.findAllByColumnValue(collectionGenericType, mappedByJoinColumnName(field), entityId));
        }

        if (isManyToMany(field)) {
            return new PersistentList<>(() ->  session.findByJoinTableField(collectionGenericType, field, entityId));
        }

        return Collections.emptyList();
    }
}
