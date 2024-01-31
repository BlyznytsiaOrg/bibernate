package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import static io.github.blyznytsiaorg.bibernate.dao.EntityDao.CANNOT_EXECUTE_SAVE_ENTITY_CLASS_MESSAGE;
import static io.github.blyznytsiaorg.bibernate.dao.EntityDao.QUERY_LOG;
import static io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder.insert;
import static io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.GenerationType.NONE;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.table;

import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoneGenerator extends AbstractGenerator implements Generator {

  private static final GenerationType TYPE = NONE;

  public NoneGenerator(
      BibernateDatabaseSettings bibernateDatabaseSettings,
      List<String> executedQueries) {
    super(bibernateDatabaseSettings, executedQueries);
  }

  @Override
  public GenerationType type() {
    return TYPE;
  }

  @Override
  public <T> Object handle(Object entity, DataSource dataSource) {
    var tableName = table(entity.getClass());
    var query = insert(entity, tableName);
    addToExecutedQueries(query);
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(query)) {
      populatePreparedStatement(entity, statement);
      showSql(() -> log.info(QUERY_LOG, query));
      statement.execute();
    } catch (Exception e) {
      throw new BibernateGeneralException(
          CANNOT_EXECUTE_SAVE_ENTITY_CLASS_MESSAGE.formatted(entity.getClass(), e.getMessage()), e);
    }
    return entity;
  }
}
