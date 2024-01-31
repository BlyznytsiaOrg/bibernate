package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import static io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder.insert;
import static io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.GenerationType.SEQUENCE;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.columnName;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.setIdField;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.table;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_EXECUTE_SAVE_ENTITY_CLASS;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_FIND_SEQUENCE_STRATEGY;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_GENERATE_ID_FOR_ENTITY;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.QUERY;

import io.github.blyznytsiaorg.bibernate.annotation.GeneratedValue;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SequenceGenerator extends AbstractGenerator implements Generator {

  private static final GenerationType TYPE = SEQUENCE;
  private final static String SEQ = "seq";
  private final static String SEPARATOR = "_";

  private final static String SELECT_NEXT_QUERY = "select nextval('%s');";

  public SequenceGenerator(
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
    Object generatedId = generateId(entity, tableName, dataSource);
    var query = insert(entity, tableName);
    addToExecutedQueries(query);
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(query)) {
      populatePreparedStatement(entity, statement, generatedId);
      showSql(() -> log.info(QUERY, query));
      statement.execute();
      setIdField(entity, generatedId);
    } catch (Exception e) {
      throw new BibernateGeneralException(
          CANNOT_EXECUTE_SAVE_ENTITY_CLASS.formatted(entity.getClass(), e.getMessage()), e);
    }
    return entity;
  }

  private String getQuery (Object entity, String tableName) {
    return Arrays.stream(entity.getClass().getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(GeneratedValue.class)
            && SEQUENCE.equals(field.getAnnotation(GeneratedValue.class).strategy()))
        .map(field -> generateGetNextQuery(field, tableName))
        .findFirst().orElseThrow(()-> new BibernateGeneralException(CANNOT_FIND_SEQUENCE_STRATEGY.formatted(entity.getClass().toString())));
  }

  private static String generateGetNextQuery(Field field, String tableName) {
    return SELECT_NEXT_QUERY.formatted(getSequenceName(field, tableName));
  }

  public static String getSequenceName(Field field, String tableName) {
    return tableName + SEPARATOR + columnName(field) + SEPARATOR + SEQ;
  }

  private Object generateId(Object entity, String tableName, DataSource dataSource) {
    log.debug("Generating ID for entity {}", entity.getClass());
    Object result = null;
    try (var connection = dataSource.getConnection()){
      String query = getQuery(entity, tableName);
      addToExecutedQueries(query);
      var statement = connection.prepareStatement(query);
      showSql(() -> log.info(QUERY, query));
      var rs = statement.executeQuery();
      if (rs.next()) {
        result = rs.getObject(1);
      }
      log.info("ID:[{}] has been generated for entity:[{}]", result, entity.getClass());
    } catch(Exception e) {
      throw new BibernateGeneralException(CANNOT_GENERATE_ID_FOR_ENTITY.formatted(entity.getClass()), e);
    }
    return result;
  }

}
