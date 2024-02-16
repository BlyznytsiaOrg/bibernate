package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.annotation.OneToOne;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;
import io.github.blyznytsiaorg.bibernate.entity.EntityPersistent;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Objects;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

/**
 * Implementation of {@link TypeFieldResolver} for resolving custom injection values for one-to-one eager fetch
 * fields of an entity class.
 * <p>
 * This resolver is responsible for determining if a given field of an entity class represents a one-to-one eager
 * fetch relationship and preparing the value to be injected into the field.
 */
public class OneToOneEagerFieldResolver implements TypeFieldResolver {

    /**
     * <p>
     * Determines if the resolver is appropriate for the given field.
     * This implementation returns {@code true} if the field is annotated with {@link OneToOne} and the fetch type is {@link FetchType#EAGER}.
     *
     * @param field the field to be evaluated for custom injection
     * @return {@code true} if the field represents a one-to-one eager fetch relationship, {@code false} otherwise
     */
    @Override
    public boolean isAppropriate(Field field) {
        return field.isAnnotationPresent(OneToOne.class) && field.getAnnotation(OneToOne.class).fetch() == FetchType.EAGER;
    }

    /**
     * <p>
     * Prepares the value to be injected into the specified field of the entity.
     * This implementation extracts the value from the result set and populates the field based on the relationship type.
     *
     * @param field            the field to receive the injected value
     * @param resultSet        the result set containing the data from which the value will be extracted
     * @param entity           the entity object to which the field belongs
     * @param entityPersistent the persistent metadata associated with the entity
     * @return the value to be injected into the field
     * @throws BibernateGeneralException if an error occurs while populating the field
     */
    @Override
    public Object prepareValueForFieldInjection(Field field,
                                                ResultSet resultSet,
                                                Object entity,
                                                EntityPersistent entityPersistent) {
        var type = field.getType();

        try {
            if (isBidirectional(field)) {
                var obj = field.getType().getDeclaredConstructor().newInstance();
                for (var declaredField : field.getType().getDeclaredFields()) {
                    if (Objects.nonNull(declaredField.getAnnotation(OneToOne.class))
                        && (field.getAnnotation(OneToOne.class).mappedBy().equals(declaredField.getName())
                            || Objects.equals(field.getName(), declaredField.getAnnotation(OneToOne.class).mappedBy()))) {
                        setField(declaredField, obj, entity);
                    } else {
                        var object = resultSet.getObject(columnName(declaredField));
                        setField(declaredField, obj, object);
                    }
                }

                return obj;
            } else {
                var obj = field.getType().getDeclaredConstructor().newInstance();
                for (var declaredField : field.getType().getDeclaredFields()) {
                    var object = resultSet.getObject(columnName(declaredField, type));

                    if (Objects.nonNull(declaredField.getAnnotation(OneToOne.class))) {
                        var currentFieldType = declaredField.getType();
                        var oneOnOneRelation = entityPersistent.toEntity(resultSet, currentFieldType);
                        setField(declaredField, obj, oneOnOneRelation);
                    } else {
                        setField(declaredField, obj, object);
                    }
                }

                return obj;
            }
        } catch (Exception exe) {
            throw new BibernateGeneralException(
                    "Cannot populate type " + field.getType() + "due to message " + exe.getMessage(), exe
            );
        }
    }
}
