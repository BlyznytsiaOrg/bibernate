package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.entity.EntityPersistent;

import java.lang.reflect.Field;
import java.sql.ResultSet;

/**
 * Interface for resolving custom injection values for fields of an entity class.
 * <p>
 * Implementations of this interface are responsible for determining if a given field of an entity class is appropriate
 * for custom injection and preparing the value to be injected into the field.
 */
public interface TypeFieldResolver {
   /**
    * Determines if the resolver is appropriate for the given field.
    *
    * @param field the field to be evaluated for custom injection
    * @return {@code true} if the resolver is appropriate for the given field, {@code false} otherwise
    */
   boolean isAppropriate(Field field);

   /**
    * Prepares the value to be injected into the specified field of the entity.
    *
    * @param field          the field to receive the injected value
    * @param resultSet      the result set containing the data from which the value will be extracted
    * @param entity         the entity object to which the field belongs
    * @param entityPersistent the persistent metadata associated with the entity
    * @return the value to be injected into the field
    */
   Object prepareValueForFieldInjection(Field field, ResultSet resultSet, Object entity, EntityPersistent entityPersistent);
}
