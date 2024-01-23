package io.github.blyznytsiaorg.bibernate.entity;

import io.github.blyznytsiaorg.bibernate.entity.type.TypeFieldResolver;
import io.github.blyznytsiaorg.bibernate.entity.type.TypeResolverFactory;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Objects;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

/**
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class EntityPersistent {
    private final TypeResolverFactory typeResolverFactory = new TypeResolverFactory();

    public <T> T toEntity(ResultSet resultSet, Class<T> entityClass)
            throws ReflectiveOperationException {
        T entity = entityClass.getDeclaredConstructor().newInstance();

        for (var field : entityClass.getDeclaredFields()) {
            typeResolverFactory.getTypeFieldResolvers().stream()
                    .filter(valueType -> valueType.isResolved(field))
                    .findAny()
                    .ifPresent(valueType -> setFieldDependency(valueType, field, entity, resultSet));
        }

        return entity;
    }

    @SneakyThrows
    private <T> void setFieldDependency(TypeFieldResolver valueType,
                                        Field field,
                                        T entity,
                                        ResultSet resultSet) {
        field.setAccessible(true);
        Object obj = field.get(entity);
        Object value = valueType.setValueToField(field, resultSet);

        if (Objects.isNull(obj)) {
            setField(field, entity, value);
        }
    }
}
