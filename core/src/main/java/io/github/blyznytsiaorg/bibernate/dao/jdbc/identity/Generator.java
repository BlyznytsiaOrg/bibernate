package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import javax.sql.DataSource;

public interface Generator {
  GenerationType type();
  Object handle(Object entity, DataSource dataSource);
}
