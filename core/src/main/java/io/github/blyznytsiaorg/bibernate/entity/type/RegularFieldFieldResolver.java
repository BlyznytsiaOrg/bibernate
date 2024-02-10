package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.utils.EntityRelationsUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;

public class RegularFieldFieldResolver implements TypeFieldResolver {
    @Override
    public boolean isAppropriate(Field field) {
        return EntityRelationsUtils.isRegularField(field);
    }

    @Override
    public Object prepareValueForFieldInjection(Field field, ResultSet resultSet, Object entityClass) {
        var fieldName = columnName(field);
        if (EntityRelationsUtils.hasOneToOneRelation(entityClass.getClass())) {
            fieldName = table(entityClass.getClass()).concat("_").concat(fieldName);
        }
        return getValueFromResultSet(field, resultSet, fieldName);
    }

}
