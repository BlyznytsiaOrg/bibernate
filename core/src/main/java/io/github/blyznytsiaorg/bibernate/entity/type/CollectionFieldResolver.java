package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.collection.PersistentList;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionContextHolder;
import io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils;
import io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

public class CollectionFieldResolver implements TypeFieldResolver {
    
    @Override
    public boolean isAppropriate(Field field) {
        return EntityRelationsUtils.isCollectionField(field);
    }

    @Override
    public Object prepareValueForFieldInjection(Field field, ResultSet resultSet) {
        Object entityId = getEntityId(field, resultSet);

        if (Objects.isNull(entityId)) {
            throw new BibernateGeneralException("Unable to get [%s] from entity [%s] without having the entity id."
                    .formatted(field.getName(), field.getDeclaringClass()));
        }
        
        var collectionGenericType = getCollectionGenericType(field); 
        var joinColumnName = joinColumnName(field);

        var session = BibernateSessionContextHolder.getBibernateSession();
        Supplier<List<?>> collectionSupplier = () -> 
                session.findAllById(collectionGenericType, joinColumnName, entityId);
        
        return new PersistentList<>(collectionSupplier);
    }
    
    private Object getEntityId(Field field, ResultSet resultSet) {
        try {
            var idFieldName = EntityReflectionUtils.columnIdName(field.getDeclaringClass());
            return resultSet.getObject(idFieldName);
        } catch (SQLException e) {
            return null;
        }
    }
}
