package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.exception.EntityNotFoundException;
import io.github.blyznytsiaorg.bibernate.exception.MissingRequiredParametersInMethod;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactoryContextHolder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * Handler for executing the "findOne" method in a simple repository.
 * Implements the {@link SimpleRepositoryMethodHandler} interface.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
public class SimpleRepositoryFindOneMethodHandler implements SimpleRepositoryMethodHandler {

    /**
     * The name of the handled method.
     */
    private static final String METHOD_NAME = "findOne";
    /**
     * Exception message for a method that looks like it is missing the required parameter ID.
     */
    private static final String LOOKS_LIKE_S_WITHOUT_REQUIRED_PARAMETER_ID = "Looks like %s  without required parameter ID";
    /**
     * Exception message for an entity not found by ID.
     */
    private static final String ENTITY_NOT_FOUND = "Entity %s not found by ID %s";

    /**
     * Checks if the given method is the "findOne" method.
     *
     * @param method The method to check.
     * @return {@code true} if the method is "findOne", {@code false} otherwise.
     */
    @Override
    public boolean isMethodHandle(Method method) {
        return method.getName().equals(METHOD_NAME);
    }

    /**
     * Executes the "findOne" method, querying the entity by its primary key.
     *
     * @param method           The "findOne" method to execute.
     * @param parameters       The parameters for the method invocation.
     * @param repositoryDetails Details about the repository, including its name, primary key type, entity type, etc.
     * @param methodMetadata   Metadata for the repository method, including its name, return type, and parameters.
     * @return The result of the "findOne" method execution.
     * @throws EntityNotFoundException If the entity is not found by the specified ID.
     * @throws IllegalArgumentException If the method parameters are not as expected.
     */
    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        var methodName = method.getName();
        log.trace(HANDLE_METHOD, methodName);
        if (parameters.length > 0) {
            var sessionFactory = BibernateSessionFactoryContextHolder.getBibernateSessionFactory();

            try (var bringSession = sessionFactory.openSession()) {
                var entityType = (Class<?>) repositoryDetails.entityType();
                var primaryKey = parameters[0];

                return bringSession.findById(entityType, primaryKey)
                        .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND.formatted(entityType.getSimpleName(), primaryKey)));
            }
        }

        throw new MissingRequiredParametersInMethod(LOOKS_LIKE_S_WITHOUT_REQUIRED_PARAMETER_ID.formatted(methodName));
    }
}
