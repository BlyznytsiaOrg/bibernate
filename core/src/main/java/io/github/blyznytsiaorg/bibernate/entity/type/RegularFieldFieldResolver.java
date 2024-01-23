package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.columnName;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getValueFromResultSet;

public class RegularFieldFieldResolver implements TypeFieldResolver {
    @Override
    public boolean isAppropriate(Field field) {
        return EntityReflectionUtils.isRegularField(field);
    }

    @Override
    public Object prepareValueForFieldInjection(Field field, ResultSet resultSet) {
        var fieldName = columnName(field);
        return getValueFromResultSet(field, resultSet, fieldName);
    }

}
