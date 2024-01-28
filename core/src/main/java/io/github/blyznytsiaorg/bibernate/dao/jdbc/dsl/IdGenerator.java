package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;


import static io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.GenerationType.SEQUENCE;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.*;
import static java.util.stream.Collectors.*;

import io.github.blyznytsiaorg.bibernate.annotation.GeneratedValue;
import io.github.blyznytsiaorg.bibernate.dao.EntityDao;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.sql.DataSource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class IdGenerator {

  private static final String CANNOT_EXECUTE_QUERY =
      "Cannot execute query [%s] message: [%s]";
  private final static String SEQ = "seq";
  private final static String SEPARATOR = "_";
  //private final static String SELECT_NEXT_QUERY = "select next value for ";
  private final static String SELECT_NEXT_QUERY = "select nextval('%s');";
  private final Map<Field, Object> generatedFields;
  private final GenerationType strategy;
  private final Map<Field, String> queries;

  public static IdGenerator createIdGenerator(Object entity, String tableName, DataSource dataSource) {
    var queries = Arrays.stream(entity.getClass().getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(GeneratedValue.class)
            && SEQUENCE.equals(field.getAnnotation(GeneratedValue.class).strategy())).collect(
        toMap(Function.identity(), field -> generateGetNextQuery(field, tableName)));
    Map<Field, Object> generatedFields = new HashMap<>();
    try (var connection = dataSource.getConnection()){
        for(var query : queries.entrySet()) {
          var statement = connection.prepareStatement(query.getValue());
          //EntityDao.showSql(() -> log.info(QUERY_LOG, .getValue()));

          var rs = statement.executeQuery();
          if (rs.next()) {
            generatedFields.put(query.getKey(), rs.getObject(1));
          }
        }
    } catch(Exception e) {
      throw new BibernateGeneralException("Cannot create IDGenerator", e);
    }

    return new IdGenerator(generatedFields, getStrategy(entity), queries);
  }

  public static String getSequenceName(Field field, String tableName) {
    return tableName + SEPARATOR + columnName(field) + SEPARATOR + SEQ;
  }


  private static String generateGetNextQuery(Field field, String tableName) {
    //return SELECT_NEXT_QUERY + getSequenceName(field, tableName);
    return SELECT_NEXT_QUERY.formatted(getSequenceName(field, tableName));
  }

  private static GenerationType getStrategy(Object entity) {
    return Arrays.stream(entity.getClass().getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(GeneratedValue.class))
        .map(field -> field.getAnnotation(GeneratedValue.class).strategy())
        .findFirst().orElse(GenerationType.NONE);
  }

  private IdGenerator(Map<Field, Object> generatedFields, GenerationType strategy, Map<Field, String> queries) {
    this.generatedFields = generatedFields;
    this.strategy = strategy;
    this.queries = queries;
  }

}
