package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.collection.PersistentList;
import io.github.blyznytsiaorg.bibernate.entity.EntityPersistent;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder;
import io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils;
import io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Objects;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getCollectionGenericType;
import static io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils.mappedByJoinColumnName;

public class CollectionFieldResolver implements TypeFieldResolver {
    
    @Override
    public boolean isAppropriate(Field field) {
        return EntityRelationsUtils.isCollectionField(field);
    }

    @Override
    public Object prepareValueForFieldInjection(Field field, ResultSet resultSet, Object entity,
                                                EntityPersistent entityPersistent) {
        Object entityId = getEntityId(field, resultSet);

        if (Objects.isNull(entityId)) {
            throw new BibernateGeneralException("Unable to get [%s] from entity [%s] without having the entity id."
                    .formatted(field.getName(), field.getDeclaringClass()));
        }
        
        var collectionGenericType = getCollectionGenericType(field);
        var session = BibernateContextHolder.getBibernateSession();

        if (EntityRelationsUtils.isOneToMany(field)) {
            return new PersistentList<>(() ->
                    session.findAllByColumnValue(collectionGenericType, mappedByJoinColumnName(field), entityId));
        }

        if (EntityRelationsUtils.isManyToMany(field)) {
            return new PersistentList<>(() ->  session.findByJoinTableField(collectionGenericType, field, entityId));
        }

        return Collections.emptyList();
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
