package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.annotation.Param;

import java.util.List;
import java.util.Optional;

/**
 * Generic interface for a basic Bibernate repository providing common CRUD operations.
 *
 * @param <T>  The type of the entity managed by the repository.
 * @param <ID> The type of the entity's primary key.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public interface BibernateRepository<T, ID> {

    /**
     * Retrieves an entity by its primary key.
     *
     * @param primaryKey The primary key of the entity to be retrieved.
     * @return An {@code Optional} containing the entity if found, or an empty {@code Optional} otherwise.
     */
    Optional<T> findById(@Param("id") ID primaryKey);

    /**
     * Retrieves an entity by its primary key.
     *
     * @param primaryKey The primary key of the entity to be retrieved.
     * @return The entity if found, or {@code null} otherwise.
     */
    T findOne(@Param("id") ID primaryKey);

    /**
     * Retrieves all entities of the managed type.
     *
     * @return A list containing all entities in the repository.
     */
    List<T> findAll();

    /**
     * Updates the given entity in the repository.
     *
     * @param entity The entity to be updated.
     */
    void update(T entity);

    /**
     * Saves the given entity in the repository.
     *
     * @param entity The entity to be saved.
     * @return The saved entity.
     */
    T save(T entity);

    /**
     * Saves a list of entities in the repository.
     *
     * @param entities The list of entities to be saved.
     */
    void saveAll(List<T> entities);

    /**
     * Deletes an entity by its primary key.
     *
     * @param primaryKey The primary key of the entity to be deleted.
     */
    void delete(@Param("id") ID primaryKey);

    /**
     * Deletes multiple entities by their primary keys.
     *
     * @param ids The list of primary keys of entities to be deleted.
     */
    void deleteAll(@Param("ids") List<ID> ids);
}
