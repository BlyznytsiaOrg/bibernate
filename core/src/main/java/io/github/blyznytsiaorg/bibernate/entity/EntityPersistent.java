package io.github.blyznytsiaorg.bibernate.entity;

import io.github.blyznytsiaorg.bibernate.entity.type.TypeFieldResolver;
import io.github.blyznytsiaorg.bibernate.entity.type.TypeResolverFactory;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.setField;

/**
 * Provides functionality for mapping data from a ResultSet to an entity object.
 * This class facilitates the process of converting database query results into
 * corresponding entity objects.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class EntityPersistent {
    private final TypeResolverFactory typeResolverFactory = new TypeResolverFactory();

    private final List<String> ignoredRelationFields = new ArrayList<>();

    /**
     * Converts data from a ResultSet into an entity object of the specified class.
     *
     * @param resultSet   The ResultSet containing the data to be mapped.
     * @param entityClass The class of the entity to be instantiated.
     * @param <T>         The type of the entity.
     * @return An instance of the entity class populated with data from the ResultSet.
     * @throws ReflectiveOperationException If an error occurs during reflection-based operations.
     */
    public <T> T toEntity(ResultSet resultSet, Class<T> entityClass) throws ReflectiveOperationException {
        T entity = entityClass.getDeclaredConstructor().newInstance();

        for (var field : entityClass.getDeclaredFields()) {
            typeResolverFactory.getTypeFieldResolvers().stream()
                    .filter(valueType -> valueType.isAppropriate(field)
                            && !ignoredRelationFields.contains(field.getName()))
                    .findFirst()
                    .ifPresent(fieldResolver -> setFieldDependency(fieldResolver, field, entity, resultSet));
        }

        return entity;
    }

    private <T> void setFieldDependency(TypeFieldResolver valueType,
                                        Field field,
                                        T entity,
                                        ResultSet resultSet) {
        Object value = valueType.prepareValueForFieldInjection(field, resultSet, entity, this);
        Optional.ofNullable(value).ifPresent(v -> setField(field, entity, v));
    }

    /**
     * Adds the specified field names to the list of ignored relation fields.
     *
     * @param fieldNames The names of the fields to be ignored.
     */
    public void addIgnoredRelationFields(List<String> fieldNames) {
        ignoredRelationFields.addAll(fieldNames);
    }

    /**
     * Clears the list of ignored relation fields.
     */
    public void clearIgnoredRelationFields() {
        ignoredRelationFields.clear();
    }
}
