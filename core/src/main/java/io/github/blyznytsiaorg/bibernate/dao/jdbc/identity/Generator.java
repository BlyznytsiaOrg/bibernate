package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import io.github.blyznytsiaorg.bibernate.annotation.GenerationType;

import javax.sql.DataSource;
import java.util.Collection;

/**
 * An interface for database generators in Bibernate application.
 * Implementations are expected to specify the generation type and provide a method to handle
 * the generation process for a given entity class and collection of entities using a data source.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public interface Generator {

    /**
     * Gets the generation type associated with the generator.
     *
     * @return the generation type
     */
    GenerationType type();

    /**
     * Handles the generation process for a given entity class and collection of entities using a data source.
     *
     * @param entityClass the class of the entity to be generated
     * @param entities    the collection of entities to be generated
     * @param dataSource  the data source for obtaining a database connection
     * @param <T>         the type of entities in the collection
     */
    <T> void handle(Class<T> entityClass, Collection<T> entities, DataSource dataSource);
}
