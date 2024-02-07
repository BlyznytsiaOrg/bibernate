package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import io.github.blyznytsiaorg.bibernate.annotation.GenerationType;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.blyznytsiaorg.bibernate.dao.jdbc.SqlBuilder.insert;
import static io.github.blyznytsiaorg.bibernate.annotation.GenerationType.SEQUENCE;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_EXECUTE_SAVE_ENTITY_CLASS;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.ExceptionMessage.CANNOT_GET_ID_FROM_SEQUENCE;
import static io.github.blyznytsiaorg.bibernate.utils.MessageUtils.LogMessage.QUERY;

@Slf4j
public class SequenceIdGenerator extends AbstractGenerator implements Generator {
  private static final String SELECT_NEXT_QUERY = "select nextval('%s');";

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
  public Object handle(Object entity, DataSource dataSource) {
    var tableName = table(entity.getClass());
    Object generatedId = generateId(entity, tableName, dataSource);
    var query = insert(entity, tableName);
    addToExecutedQueries(query);
    try (var connection = dataSource.getConnection();
        var statement = connection.prepareStatement(query)) {
      populatePreparedStatement(entity, statement, generatedId);
      showSql(() -> log.debug(QUERY, query));
      statement.execute();
      setIdField(entity, generatedId);
    } catch (Exception e) {
      throw new BibernateGeneralException(
          CANNOT_EXECUTE_SAVE_ENTITY_CLASS.formatted(entity.getClass(), e.getMessage()), e);
    }
    return entity;
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
      showSql(() -> log.debug(QUERY, query));
      var rs = statement.executeQuery();
      if (rs.next()) {
        result = rs.getLong(1);
      }
      log.debug("Next ID:[{}] was fetched from db for sequence:[{}]", result, sequenceName);
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
