package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.session.BibernateSessionContextHolder;
import io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getValueFromResultSetByColumn;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.joinColumnName;

public class EntityFieldFieldResolver implements TypeFieldResolver {

    @Override
    public boolean isAppropriate(Field field) {
        return EntityReflectionUtils.isEntityField(field);
    }

    @Override
    public Object prepareValueForFieldInjection(Field field, ResultSet resultSet) {
        var session = BibernateSessionContextHolder.getBibernateSession();
        var joinColumnName = joinColumnName(field);
        var joinColumnValue = getValueFromResultSetByColumn(resultSet, joinColumnName);

        return session.findById(field.getType(), joinColumnValue)
                .orElse(null);
    }
}
