package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactoryContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Handler for executing the deleteAll method in a simple repository.
 * Implements the {@link SimpleRepositoryMethodHandler} interface.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
public class SimpleRepositoryMethodDeleteAllHandler implements SimpleRepositoryMethodHandler {
    /**
     * The name of the handled method (deleteAll).
     */
    private static final String METHOD_NAME = "deleteAll";
    /**
     * Message indicating that the deleteAll method should have one parameter List<Id>.
     */
    private static final String DELETE_METHOD_SHOULD_HAVE_ONE_PARAMETER_ID =
            "DeleteAll method should have one parameter List<Id>";

    /**
     * Checks if the given method is the deleteAll method.
     *
     * @param method The method to check.
     * @return {@code true} if the method is deleteAll, {@code false} otherwise.
     */
    @Override
    public boolean isMethodHandle(Method method) {
        return method.getName().equals(METHOD_NAME);
    }

    /**
     * Executes the deleteAll method in the simple repository.
     *
     * @param method           The deleteAll method to execute.
     * @param parameters       The parameters for the deleteAll method invocation.
     * @param repositoryDetails Details about the repository, including its name, primary key type, entity type, etc.
     * @param methodMetadata   Metadata for the repository method, including its name, return type, and parameters.
     * @return The result of the deleteAll method execution, which is always {@code Void.TYPE}.
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
                var parameter = parameters[0];
                if (parameter instanceof List<?> ids) {
                    //TODO change the to batch later
                    ids.forEach(id -> bringSession.deleteById(entityClass, id));
                    return Void.TYPE;
                }
            }
        }

        log.warn(DELETE_METHOD_SHOULD_HAVE_ONE_PARAMETER_ID);
        return null;
    }
}
