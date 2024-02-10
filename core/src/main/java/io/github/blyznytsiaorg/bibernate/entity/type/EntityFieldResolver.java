package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import static io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder.getBibernateSession;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getValueFromResultSetByColumn;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.joinColumnName;

@Slf4j
public class EntityFieldResolver implements TypeFieldResolver {

    @Override
    public boolean isAppropriate(Field field) {
        return EntityRelationsUtils.isEntityField(field);
    }

    @Override
    public Object prepareValueForFieldInjection(Field field, ResultSet resultSet, Object entity) {
        var session = getBibernateSession();
        var joinColumnName = joinColumnName(field);
        var joinColumnValue = getValueFromResultSetByColumn(resultSet, joinColumnName);

        return session.findById(field.getType(), joinColumnValue)
                .orElse(null);
    }
}
