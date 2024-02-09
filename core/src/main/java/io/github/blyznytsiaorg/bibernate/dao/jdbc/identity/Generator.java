package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import io.github.blyznytsiaorg.bibernate.annotation.GenerationType;
import javax.sql.DataSource;
import java.util.Collection;

public interface Generator {
  GenerationType type();
  <T> void handle(Class<T> entityClass, Collection<T> entity, DataSource dataSource);
}
