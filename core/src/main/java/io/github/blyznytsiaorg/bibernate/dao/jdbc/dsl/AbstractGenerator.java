package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getGeneratedValueField;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getInsertEntityFields;
import static io.github.blyznytsiaorg.bibernate.utils.EntityReflectionUtils.getValueFromObject;

import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public abstract class AbstractGenerator {

  private final BibernateDatabaseSettings bibernateDatabaseSettings;
  private final List<String> executedQueries;

  public AbstractGenerator(BibernateDatabaseSettings bibernateDatabaseSettings,
      List<String> executedQueries) {
    this.bibernateDatabaseSettings = bibernateDatabaseSettings;
    this.executedQueries = executedQueries;
  }

  protected void addToExecutedQueries(String query) {
    if (bibernateDatabaseSettings.isCollectQueries()) {
      executedQueries.add(query);
    }
  }

  protected void showSql(Runnable logSql) {
    if (bibernateDatabaseSettings.isShowSql()) {
      logSql.run();
    }
  }
  protected void populatePreparedStatement(Object entity, PreparedStatement statement, Object generatedId) throws SQLException {
    int index = 1;
    Field generatedValueField = getGeneratedValueField(entity);
    for (Field field : getInsertEntityFields(entity)) {
      statement.setObject(index++, getGeneratedValue(field, generatedValueField, generatedId).orElse(getValueFromObject(entity, field)));
    }
  }
  protected void populatePreparedStatement(Object entity, PreparedStatement statement) throws SQLException {
    int index = 1;
    for (Field field : getInsertEntityFields(entity)) {
      statement.setObject(index++, getValueFromObject(entity, field));
    }
  }

  protected static Optional<Object> getGeneratedValue (Field field, Field generatedValueField, Object generatedId) {
    return field.equals(generatedValueField) ? Optional.ofNullable(generatedId) : Optional.empty();
  }
}
