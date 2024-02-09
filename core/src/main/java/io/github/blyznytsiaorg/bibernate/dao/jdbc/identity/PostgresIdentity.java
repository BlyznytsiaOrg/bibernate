package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import io.github.blyznytsiaorg.bibernate.annotation.GeneratedValue;
import io.github.blyznytsiaorg.bibernate.annotation.GenerationType;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Blyzhnytsia Team
 * @since 1.0
 */
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
    public <T> void saveWithIdentity(Class<T> entityClass, Collection<T> entities) {
        var strategy = getStrategy(entityClass);
        for (var generator : generators) {
            if (strategy.equals(generator.type())) {
                generator.handle(entityClass, entities, bibernateDatabaseSettings.getDataSource());
            }
        }
    }

    private static GenerationType getStrategy(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(GeneratedValue.class))
                .map(field -> field.getAnnotation(GeneratedValue.class).strategy())
                .findFirst().orElse(GenerationType.NONE);
    }
}
