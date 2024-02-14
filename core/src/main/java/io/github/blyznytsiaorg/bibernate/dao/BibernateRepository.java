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
     * <pre>{@code
     * Optional<User> user = userRepository.findById(123);
     * if (user.isPresent()) {
     *     System.out.println("User found: " + user.get());
     * } else {
     *     System.out.println("User not found");
     * }
     * }</pre>
     */
    Optional<T> findById(@Param("id") ID primaryKey);

    /**
     * Retrieves an entity by its primary key.
     *
     * @param primaryKey The primary key of the entity to be retrieved.
     * @return The entity if found, or {@code null} otherwise.
     * <pre>{@code
     * User user = userRepository.findOne(123);
     * if (user != null) {
     *     System.out.println("User found: " + user);
     * } else {
     *     System.out.println("User not found");
     * }
     * }</pre>
     */
    T findOne(@Param("id") ID primaryKey);

    /**
     * Retrieves all entities of the managed type.
     *
     * @return A list containing all entities in the repository.
     * <pre>{@code
     * List<User> users = userRepository.findAll();
     * for (User user : users) {
     *     System.out.println("User: " + user);
     * }
     * }</pre>
     */
    List<T> findAll();

    /**
     * Updates the given entity in the repository.
     *
     * @param entity The entity to be updated.
     * <pre>{@code
     * User user = userRepository.findOne(123);
     * user.setName("John Doe");
     * userRepository.update(user);
     * }</pre>
     */
    void update(T entity);

    /**
     * Saves the given entity in the repository.
     *
     * @param entity The entity to be saved.
     * @return The saved entity.
     * <pre>{@code
     * User user = new User("Alice");
     * userRepository.save(user);
     * }</pre>
     */
    T save(T entity);

    /**
     * Saves a list of entities in the repository.
     *
     * @param entities The list of entities to be saved.
     * <pre>{@code
     * List<User> users = Arrays.asList(new User("Alice"), new User("Bob"));
     * userRepository.saveAll(users);
     * }</pre>
     */
    void saveAll(List<T> entities);

    /**
     * Deletes an entity by its primary key.
     *
     * @param primaryKey The primary key of the entity to be deleted.
     * <pre>{@code
     * userRepository.delete(123);
     * }</pre>
     */
    void delete(@Param("id") ID primaryKey);

    /**
     * Deletes multiple entities by their primary keys.
     *
     * @param ids The list of primary keys of entities to be deleted.
     * <pre>{@code
     * List<Integer> userIds = Arrays.asList(123, 456, 789);
     * userRepository.deleteAll(userIds);
     * }</pre>
     */
    void deleteAll(@Param("ids") List<ID> ids);
}
