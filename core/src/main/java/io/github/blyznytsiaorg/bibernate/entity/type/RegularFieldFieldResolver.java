package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.columnName;

public class RegularFieldFieldResolver implements TypeFieldResolver {
    @Override
    public boolean isResolved(Field field) {
        return EntityReflectionUtils.isRegularField(field);
    }

    @Override
    public Object setValueToField(Field field, ResultSet resultSet) throws SQLException {
        var fieldName = columnName(field);
        return resultSet.getObject(fieldName, field.getType());
    }
}
