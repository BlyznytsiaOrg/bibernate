package io.github.blyznytsiaorg.bibernate.entity.type;

import io.github.blyznytsiaorg.bibernate.entity.EntityPersistent;

import java.lang.reflect.Field;
import java.sql.ResultSet;

public interface TypeFieldResolver {
   boolean isAppropriate(Field field);

   Object prepareValueForFieldInjection(Field field, ResultSet resultSet, Object entity, EntityPersistent entityPersistent);
}
