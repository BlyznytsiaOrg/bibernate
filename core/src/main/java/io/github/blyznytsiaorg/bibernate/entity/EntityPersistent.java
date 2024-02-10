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
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class EntityPersistent {
    private final TypeResolverFactory typeResolverFactory = new TypeResolverFactory();

    private final List<String> ignoredRelationFields = new ArrayList<>();

    public <T> T toEntity(ResultSet resultSet, Class<T> entityClass) throws ReflectiveOperationException  {
        
        T entity = entityClass.getDeclaredConstructor().newInstance();

        for (var field : entityClass.getDeclaredFields()) {
            typeResolverFactory.getTypeFieldResolvers().stream()
                    .filter(valueType -> valueType.isAppropriate(field)
                            && !ignoredRelationFields.contains(field.getName()))
                    .findFirst()
                    .ifPresent(fieldResolver -> setFieldDependency(fieldResolver, field, entity, resultSet, entityClass));
        }

        return entity;
    }

    private <T> void setFieldDependency(TypeFieldResolver valueType,
                                        Field field,
                                        T entity,
                                        ResultSet resultSet,
                                        Class<T> entityClass) {
        Object value = valueType.prepareValueForFieldInjection(field, resultSet, entityClass);
        Optional.ofNullable(value).ifPresent(v -> setField(field, entity, v));
    }

    public void addIgnoredRelationFields(List<String> fieldNames) {
        ignoredRelationFields.addAll(fieldNames);
    }

    public void clearIgnoredRelationFields() {
        ignoredRelationFields.clear();
    }
    
}
