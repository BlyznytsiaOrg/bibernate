package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import javax.sql.DataSource;

public interface Generator {
  GenerationType type();
  <T> Object handle(Object entity, DataSource dataSource);

}
