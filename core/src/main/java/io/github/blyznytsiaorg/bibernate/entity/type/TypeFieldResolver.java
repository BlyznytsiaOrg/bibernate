package io.github.blyznytsiaorg.bibernate.entity.type;

import java.lang.reflect.Field;
import java.sql.ResultSet;

public interface TypeFieldResolver {
   boolean isAppropriate(Field field);

   Object prepareValueForFieldInjection(Field field, ResultSet resultSet);
}
