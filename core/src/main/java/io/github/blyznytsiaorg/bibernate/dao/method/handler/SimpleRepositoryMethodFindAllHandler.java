package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactoryContextHolder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.nonNull;

/**
 * Handler for executing the findAll method in a simple repository.
 * Implements the {@link SimpleRepositoryMethodHandler} interface.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
public class SimpleRepositoryMethodFindAllHandler implements SimpleRepositoryMethodHandler {

    /**
     * The name of the handled method (findAll).
     */
    private static final String METHOD_NAME = "findAll";

    /**
     * Checks if the given method is the findAll method.
     *
     * @param method The method to check.
     * @return {@code true} if the method is findAll, {@code false} otherwise.
     */
    @Override
    public boolean isMethodHandle(Method method) {
        return method.getName().equals(METHOD_NAME);
    }

    /**
     * Executes the findAll method in the simple repository.
     *
     * @param method           The findAll method to execute.
     * @param parameters       The parameters for the findAll method invocation.
     * @param repositoryDetails Details about the repository, including its name, primary key type, entity type, etc.
     * @param methodMetadata   Metadata for the repository method, including its name, return type, and parameters.
     * @return The result of the findAll method execution, a list of entities or an empty list if the return type is not supported.
     */
    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        var methodName = method.getName();
        log.trace(HANDLE_METHOD, methodName);
        var returnType = methodMetadata.getReturnType();

        if (nonNull(returnType.getGenericEntityClass()) &&
                List.class.isAssignableFrom((Class<?>) returnType.getGenericEntityClass().getRawType())) {
            var sessionFactory = BibernateSessionFactoryContextHolder.getBibernateSessionFactory();
            try (var bringSession = sessionFactory.openSession()) {
                var entityClass  = (Class<?>) repositoryDetails.entityType();
                return bringSession.findByWhere(entityClass, null, parameters);
            }
        }

        log.warn(NOT_SUPPORTED_RETURN_TYPE_FOR_METHOD_NAME, returnType, methodName);
        return Collections.emptyList();
    }
}
