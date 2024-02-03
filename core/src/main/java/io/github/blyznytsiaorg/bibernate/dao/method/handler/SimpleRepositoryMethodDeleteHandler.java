package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactoryContextHolder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * Handler for executing the delete method in a simple repository.
 * Implements the {@link SimpleRepositoryMethodHandler} interface.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
public class SimpleRepositoryMethodDeleteHandler implements SimpleRepositoryMethodHandler {
    /**
     * The name of the handled method (delete).
     */
    private static final String METHOD_NAME = "delete";

    /**
     * Checks if the given method is the delete method.
     *
     * @param method The method to check.
     * @return {@code true} if the method is delete, {@code false} otherwise.
     */
    @Override
    public boolean isMethodHandle(Method method) {
        return method.getName().equals(METHOD_NAME);
    }

    /**
     * Executes the delete method in the simple repository.
     *
     * @param method           The delete method to execute.
     * @param parameters       The parameters for the delete method invocation.
     * @param repositoryDetails Details about the repository, including its name, primary key type, entity type, etc.
     * @param methodMetadata   Metadata for the repository method, including its name, return type, and parameters.
     * @return The result of the delete method execution, which is always {@code Void.TYPE}.
     */
    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        var methodName = method.getName();
        log.trace(HANDLE_METHOD, methodName);
        if (parameters.length > 0) {
            var sessionFactory = BibernateSessionFactoryContextHolder.getBibernateSessionFactory();
            try (var bringSession = sessionFactory.openSession()) {
                var entityClass  = (Class<?>) repositoryDetails.entityType();
                bringSession.deleteById(entityClass, parameters[0]);
                return Void.TYPE;
            }
        }

        log.warn(LOOKS_LIKE_METHOD_WITHOUT_REQUIRED_PARAMETER_ID.formatted(methodName));
        return null;
    }
}
