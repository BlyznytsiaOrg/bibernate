package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import static io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder.insert;
import static io.github.blyznytsiaorg.bibernate.annotation.GenerationType.NONE;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.table;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_EXECUTE_SAVE_ENTITY_CLASS;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.QUERY;

import io.github.blyznytsiaorg.bibernate.annotation.GenerationType;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoneIdGenerator extends AbstractGenerator implements Generator {

  public NoneIdGenerator(
      BibernateDatabaseSettings bibernateDatabaseSettings,
      List<String> executedQueries) {
    super(bibernateDatabaseSettings, executedQueries);
  }

  @Override
  public GenerationType type() {
    return NONE;
  }

  @Override
  public Object handle(Object entity, DataSource dataSource) {
    var tableName = table(entity.getClass());
    var query = insert(entity, tableName);
    addToExecutedQueries(query);

    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(query)) {
      populatePreparedStatement(entity, statement);
      showSql(() -> log.debug(QUERY, query));
      statement.execute();
    } catch (Exception e) {
      throw new BibernateGeneralException(
          CANNOT_EXECUTE_SAVE_ENTITY_CLASS.formatted(entity.getClass(), e.getMessage()), e);
    }

    return entity;
  }
}
