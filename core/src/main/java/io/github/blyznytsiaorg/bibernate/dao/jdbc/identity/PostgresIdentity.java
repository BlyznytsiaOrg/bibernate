package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import io.github.blyznytsiaorg.bibernate.annotation.GeneratedValue;
import io.github.blyznytsiaorg.bibernate.annotation.GenerationType;
import io.github.blyznytsiaorg.bibernate.config.BibernateDatabaseSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of the Identity interface for managing identity-related operations
 * in Bibernate application specific to PostgreSQL databases.
 * It delegates entity-saving operations to different generators based on the GenerationType strategy
 * specified in the entities' annotations.
 * Supports NONE, SEQUENCE, and IDENTITY GenerationType strategies.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class PostgresIdentity implements Identity {

    /**
     * List of generators for handling different GenerationType strategies.
     */
    private final List<Generator> generators = new ArrayList<>();

    /**
     * The settings for Bibernate database interactions.
     */
    private final BibernateDatabaseSettings bibernateDatabaseSettings;

    /**
     * Constructs a new PostgresIdentity with the given BibernateDatabaseSettings and list of executed queries.
     *
     * @param bibernateDatabaseSettings the settings for Bibernate database interactions
     * @param executedQueries           the list to store executed queries, if query collection is enabled
     */
    public PostgresIdentity(BibernateDatabaseSettings bibernateDatabaseSettings, List<String> executedQueries) {
        this.bibernateDatabaseSettings = bibernateDatabaseSettings;
        generators.add(new NoneIdGenerator(bibernateDatabaseSettings, executedQueries));
        generators.add(new SequenceIdGenerator(bibernateDatabaseSettings, executedQueries));
        generators.add(new IdentityIdGenerator(bibernateDatabaseSettings, executedQueries));
    }

    /**
     * Implements the saveWithIdentity method from the Identity interface.
     * Delegates the entity-saving operation to the appropriate generator based on the GenerationType strategy.
     */
    @Override
    public <T> void saveWithIdentity(Class<T> entityClass, Collection<T> entities) {
        var strategy = getStrategy(entityClass);
        for (var generator : generators) {
            if (strategy.equals(generator.type())) {
                generator.handle(entityClass, entities, bibernateDatabaseSettings.getDataSource());
            }
        }
    }

    /**
     * Retrieves the GenerationType strategy specified in the entity class annotations.
     *
     * @param entityClass the class of the entity
     * @return the GenerationType strategy, or GenerationType.NONE if not specified
     */
    private static GenerationType getStrategy(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(GeneratedValue.class))
                .map(field -> field.getAnnotation(GeneratedValue.class).strategy())
                .findFirst().orElse(GenerationType.NONE);
    }
}
