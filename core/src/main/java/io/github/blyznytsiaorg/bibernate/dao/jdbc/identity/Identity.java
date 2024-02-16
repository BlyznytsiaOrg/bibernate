package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import java.util.Collection;

/**
 * An interface for handling identity-related operations in Bibernate application.
 * Implementations are expected to provide a method for saving entities with identity information.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public interface Identity {

    /**
     * Saves entities of a given class with identity information.
     *
     * @param entityClass the class of the entities to be saved
     * @param entities    the collection of entities to be saved
     * @param <T>         the type of entities in the collection
     */
    <T> void saveWithIdentity(Class<T> entityClass, Collection<T> entities);
}
