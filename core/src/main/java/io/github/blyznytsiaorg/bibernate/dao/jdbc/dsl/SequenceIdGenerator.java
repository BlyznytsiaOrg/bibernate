package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import static io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder.insert;
import static io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.GenerationType.SEQUENCE;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.columnName;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getGeneratedValueSequenceConfig;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getGeneratedValueSequenceStrategyField;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.setIdField;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.table;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_EXECUTE_SAVE_ENTITY_CLASS;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_GET_ID_FROM_SEQUENCE;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.QUERY;

import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SequenceIdGenerator extends AbstractGenerator implements Generator {

  private final static String SEQ = "seq";
  private final static String SEPARATOR = "_";

  private final static String SELECT_NEXT_QUERY = "select nextval('%s');";

  private final Map<Class<?>, SequenceConf> sequences = new HashMap<>();

  public SequenceIdGenerator(
      BibernateDatabaseSettings bibernateDatabaseSettings,
      List<String> executedQueries) {
    super(bibernateDatabaseSettings, executedQueries);
  }

  @Override
  public GenerationType type() {
    return SEQUENCE;
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
    Field field = getGeneratedValueSequenceStrategyField(entity);
    return generateGetNextQuery(field, tableName);
  }

  private static String generateGetNextQuery(Field field, String tableName) {
    return SELECT_NEXT_QUERY.formatted(getSequenceName(field, tableName));
  }

  public static String getSequenceName(Field field, String tableName) {
    return tableName + SEPARATOR + columnName(field) + SEPARATOR + SEQ;
  }

  private Object generateId(Object entity, String tableName, DataSource dataSource) {
    log.debug("Generating ID for entity {}", entity.getClass());
    var seqConf = getSequenceConf(entity, tableName);
    Long id = seqConf.getNextId();
    if(id == null) {
      seqConf.setNextPortionOfIds(getNextIdFromDbSeq(dataSource, seqConf.getName()));
      id = seqConf.getNextId();
    }
    return id;
  }

  private Long getNextIdFromDbSeq(DataSource dataSource, String sequenceName) {
    Long result = null;
    try (var connection = dataSource.getConnection()){
      var query = SELECT_NEXT_QUERY.formatted(sequenceName);
      addToExecutedQueries(query);
      var statement = connection.prepareStatement(query);
      showSql(() -> log.info(QUERY, query));
      var rs = statement.executeQuery();
      if (rs.next()) {
        result = rs.getLong(1);
      }
      log.info("Next ID:[{}] was fetched from db for sequence:[{}]", result, sequenceName);
    } catch(Exception e) {
      throw new BibernateGeneralException(CANNOT_GET_ID_FROM_SEQUENCE.formatted(sequenceName), e);
    }
    return result;
  }

  private SequenceConf getSequenceConf(Object entity, String tableName) {
    var seqConf = sequences.get(entity.getClass());
    if (seqConf == null) {
      seqConf = getGeneratedValueSequenceConfig(entity, tableName);
      sequences.put(entity.getClass(), seqConf);
    }
    return seqConf;
  }
}
