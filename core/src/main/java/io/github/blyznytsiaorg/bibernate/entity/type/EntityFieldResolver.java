package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.session.BibernateSessionContextHolder;
import io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

public class EntityFieldResolver implements TypeFieldResolver {

    @Override
    public boolean isAppropriate(Field field) {
        return EntityRelationsUtils.isEntityField(field);
    }

    @Override
    public Object prepareValueForFieldInjection(Field field, ResultSet resultSet) {
        var session = BibernateSessionContextHolder.getBibernateSession();

        if (isBidirectionalOwnerSide(field)) {
            var columnIdName = columnIdName(field.getDeclaringClass());
            var idValue = getValueFromResultSetByColumn(resultSet, columnIdName);
            return session.findAllById(field.getType(), mappedByEntityJoinColumnName(field), idValue)
                    .get(0);
        }

        var joinColumnName = joinColumnName(field);
        var joinColumnValue = getValueFromResultSetByColumn(resultSet, joinColumnName);
        return session.findById(field.getType(), joinColumnValue)
                .orElse(null);
    }
}
