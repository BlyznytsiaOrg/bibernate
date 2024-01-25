package io.github.blyznytsiaorg.bibernate.entity;

import io.github.blyznytsiaorg.bibernate.entity.type.TypeFieldResolver;
import io.github.blyznytsiaorg.bibernate.entity.type.TypeResolverFactory;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

/**
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class EntityPersistent {
    private final TypeResolverFactory typeResolverFactory = new TypeResolverFactory();

    public <T> T toEntity(ResultSet resultSet, Class<T> entityClass) throws ReflectiveOperationException  {
        
        T entity = entityClass.getDeclaredConstructor().newInstance();

        for (var field : entityClass.getDeclaredFields()) {
            Optional<TypeFieldResolver> typeFieldResolver = typeResolverFactory.getTypeFieldResolvers().stream()
                    .filter(valueType -> valueType.isAppropriate(field))
                    .findAny();
            if (typeFieldResolver.isPresent()) {
                setFieldDependency(typeFieldResolver.get(), field, entity, resultSet);
            }
        }

        return entity;
    }

    private <T> void setFieldDependency(TypeFieldResolver valueType,
                                        Field field,
                                        T entity,
                                        ResultSet resultSet) throws IllegalAccessException {
        if (shouldFieldBeSet(field, entity)) {
            Object value = valueType.prepareValueForFieldInjection(field, resultSet);
            setField(field, entity, value);
        }
    }
    
    private <T> boolean shouldFieldBeSet(Field field, T entity) throws IllegalAccessException {
        field.setAccessible(true);
        
        Object obj = field.get(entity);
        return Objects.isNull(obj) || (isSupportedCollection(field) && ((Collection<?>) obj).isEmpty());
    }
}
