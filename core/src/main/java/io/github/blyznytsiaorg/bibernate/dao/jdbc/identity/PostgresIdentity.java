package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import io.github.blyznytsiaorg.bibernate.annotation.GeneratedValue;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostgresIdentity implements Identity {
  private final List<Generator> generators = new ArrayList<>();
  private final BibernateDatabaseSettings bibernateDatabaseSettings;

  public PostgresIdentity(BibernateDatabaseSettings bibernateDatabaseSettings, List<String> executedQueries) {
    this.bibernateDatabaseSettings = bibernateDatabaseSettings;
    generators.add(new NoneIdGenerator(bibernateDatabaseSettings, executedQueries));
    generators.add(new SequenceIdGenerator(bibernateDatabaseSettings, executedQueries));
    generators.add(new IdentityIdGenerator(bibernateDatabaseSettings, executedQueries));
  }

  @Override
  public Object saveWithIdentity(Object entity) {
    GenerationType strategy = getStrategy(entity);
    for (Generator generator:generators) {
      if(strategy.equals(generator.type())){
        generator.handle(entity, bibernateDatabaseSettings.getDataSource());
      }
    }
    return entity;
  }

  private static GenerationType getStrategy(Object entity) {
    return Arrays.stream(entity.getClass().getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(GeneratedValue.class))
        .map(field -> field.getAnnotation(GeneratedValue.class).strategy())
        .findFirst().orElse(GenerationType.NONE);
  }
}
