package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.session.BibernateSessionContextHolder;
import io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

public class EntityFieldResolver implements TypeFieldResolver {

    public static final String SELECT_QUERY = "%s = ?";

    @Override
    public boolean isAppropriate(Field field) {
        return EntityRelationsUtils.isEntityField(field);
    }

    @Override
    public Object prepareValueForFieldInjection(Field field, ResultSet resultSet) {
        var session = BibernateSessionContextHolder.getBibernateSession();
        var joinColumnName = joinColumnName(field);
        var joinColumnValue = getValueFromResultSetByColumn(resultSet, joinColumnName);

//        if (isBidirectionalOwnerSide(field)) {
//            Class<?> type = field.getType();
//            Class<?> declaringClass = field.getDeclaringClass();
//            var columnIdName = columnIdName(declaringClass);
//            var idValue = getValueFromResultSetByColumn(resultSet, columnIdName);
//            return session.findByWhere(type, mappedByJoinColumnName(field) + " = ?", idValue);
//        }

        return session.findById(field.getType(), joinColumnValue)
                .orElse(null);
    }
}
