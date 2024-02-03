package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactoryContextHolder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Optional;

import static java.util.Objects.nonNull;

/**
 * Handler for executing the "findById" method in a simple repository.
 * Implements the {@link SimpleRepositoryMethodHandler} interface.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
public class SimpleRepositoryFindByIdMethodHandler implements SimpleRepositoryMethodHandler {

    /**
     * The name of the handled method.
     */
    private static final String METHOD_NAME = "findById";

    /**
     * Checks if the given method is the "findById" method.
     *
     * @param method The method to check.
     * @return {@code true} if the method is "findById", {@code false} otherwise.
     */
    @Override
    public boolean isMethodHandle(Method method) {
        return method.getName().equals(METHOD_NAME);
    }

    /**
     * Executes the "findById" method, querying the entity by its primary key.
     *
     * @param method           The "findById" method to execute.
     * @param parameters       The parameters for the method invocation.
     * @param repositoryDetails Details about the repository, including its name, primary key type, entity type, etc.
     * @param methodMetadata   Metadata for the repository method, including its name, return type, and parameters.
     * @return The result of the "findById" method execution.
     * @throws IllegalArgumentException If the method parameters are not as expected.
     */
    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        String methodName = method.getName();
        log.trace(HANDLE_METHOD, methodName);
        if (parameters.length > 0) {
            var sessionFactory = BibernateSessionFactoryContextHolder.getBibernateSessionFactory();
            var entityClass = (Class<?>) repositoryDetails.entityType();
            try (var bringSession = sessionFactory.openSession()) {
                var returnType = methodMetadata.getReturnType();
                var primaryKey = parameters[0];

                if (nonNull(returnType.getEntityClass())) {
                    return bringSession.findById(entityClass, primaryKey);
                } else if (Optional.class.isAssignableFrom((Class<?>)returnType.getGenericEntityClass().getRawType())) {
                    return bringSession.findById(entityClass, primaryKey);
                } else {
                    throw new IllegalArgumentException(CANNOT_RETURN_S_SHOULD_BE_OPTIONAL_S_OR_S.formatted(
                            returnType.getGenericEntityClass(),
                            repositoryDetails.entityType(),
                            repositoryDetails.entityType()));
                }
            }
        }

        throw new IllegalArgumentException(LOOKS_LIKE_METHOD_WITHOUT_REQUIRED_PARAMETER_ID.formatted(methodName));
    }
}
