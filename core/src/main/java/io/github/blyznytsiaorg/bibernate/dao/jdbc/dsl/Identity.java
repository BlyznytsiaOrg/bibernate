package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import java.sql.PreparedStatement;

public interface Identity {

  <T> Object generateIdEntity(Class<T> entityClass, Object entity);
}
