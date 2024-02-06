package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import io.github.blyznytsiaorg.bibernate.annotation.GenerationType;
import javax.sql.DataSource;

public interface Generator {
  GenerationType type();
  Object handle(Object entity, DataSource dataSource);
}
