package io.github.blyznytsiaorg.bibernate.entity.type;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface TypeFieldResolver {
   boolean isResolved(Field field);

   Object setValueToField(Field field, ResultSet resultSet) throws SQLException;
}
