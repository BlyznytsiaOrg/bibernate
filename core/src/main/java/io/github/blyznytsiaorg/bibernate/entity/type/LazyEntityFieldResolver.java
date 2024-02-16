package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.annotation.ManyToOne;
import io.github.blyznytsiaorg.bibernate.annotation.OneToOne;
import io.github.blyznytsiaorg.bibernate.annotation.enumeration.FetchType;
import io.github.blyznytsiaorg.bibernate.entity.EntityPersistent;
import io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder;
import io.github.blyznytsiaorg.bibernate.utils.ProxyUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.function.Supplier;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getValueFromResultSetByColumn;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.joinColumnName;

/**
 * Implementation of {@link TypeFieldResolver} for resolving custom injection values for lazy-loaded entity fields.
 * <p>
 * This resolver is responsible for determining if a given field of an entity class represents a lazy-loaded relationship
 * and preparing the value to be injected into the field.
 */
public class LazyEntityFieldResolver implements TypeFieldResolver {

    /**
     * <p>
     * Determines if the resolver is appropriate for the given field.
     * This implementation checks if the field is annotated with {@link ManyToOne} or {@link OneToOne} with lazy fetch type.
     *
     * @param field the field to be evaluated for custom injection
     * @return {@code true} if the field represents a lazy-loaded relationship, {@code false} otherwise
     */
    @Override
    public boolean isAppropriate(Field field) {
        return Optional.ofNullable(field.getAnnotation(ManyToOne.class))
                .map(annotation -> annotation.fetch() == FetchType.LAZY)
                .orElse(Optional.ofNullable(field.getAnnotation(OneToOne.class))
                        .map(annotation -> annotation.fetch() == FetchType.LAZY)
                        .orElse(false));
    }

    /**
     * <p>
     * Prepares the value to be injected into the specified field of the entity.
     * This implementation creates a lazy-loading proxy for the entity field, resolving the entity value when accessed.
     *
     * @param field            the field to receive the injected value
     * @param resultSet        the result set containing the data from which the value will be extracted
     * @param entity           the entity object to which the field belongs
     * @param entityPersistent the persistent metadata associated with the entity
     * @return the lazy-loading proxy for the entity field
     */
    @Override
    public Object prepareValueForFieldInjection(Field field,
                                                ResultSet resultSet,
                                                Object entity,
                                                EntityPersistent entityPersistent) {
        var session = BibernateContextHolder.getBibernateSession();

        var joinColumnName = joinColumnName(field);
        var joinColumnValue = getValueFromResultSetByColumn(resultSet, joinColumnName);
        var type = field.getType();
        Supplier<?> entitySupplier = () -> session.findById(type, joinColumnValue).orElse(null);

        return ProxyUtils.createProxy(type, entitySupplier);
    }
}
