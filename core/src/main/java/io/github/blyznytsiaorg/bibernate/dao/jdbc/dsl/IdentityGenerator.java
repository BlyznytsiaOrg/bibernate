package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import static io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder.insert;
import static io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.GenerationType.IDENTITY;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.setIdField;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.table;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_EXECUTE_SAVE_ENTITY_CLASS;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.QUERY;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import java.sql.ResultSet;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IdentityGenerator extends AbstractGenerator implements Generator {

  private static final GenerationType TYPE = IDENTITY;

  public IdentityGenerator(
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
        var statement = connection.prepareStatement(query, RETURN_GENERATED_KEYS)) {
        populatePreparedStatement(entity, statement);
      showSql(() -> log.info(QUERY, query));
      statement.execute();
      ResultSet generatedKeys = statement.getGeneratedKeys();
      if (generatedKeys.next()) {
        setIdField(entity, generatedKeys.getObject(1));
      }
    } catch (Exception e) {
      throw new BibernateGeneralException(
          CANNOT_EXECUTE_SAVE_ENTITY_CLASS.formatted(entity.getClass(), e.getMessage()), e);
    }
    return entity;
  }

}
