package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.exception.MissingRequiredParametersInMethod;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactoryContextHolder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * Implementation of {@link SimpleRepositoryMethodHandler} for handling the "update" method.
 * This handler updates an entity using the Bibernate session.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
public class SimpleRepositoryMethodUpdateHandler implements SimpleRepositoryMethodHandler {

    /**
     * The prefix for the handled method (update).
     */
    private static final String METHOD_NAME = "update";
    private static final String UPDATE_METHOD_SHOULD_HAVE_ONE_PARAMETER_ID = "Update method should have one parameter ID";

    /**
     * {@inheritDoc}
     * Checks if the method name starts with "update".
     */
    @Override
    public boolean isMethodHandle(Method method) {
        return method.getName().startsWith(METHOD_NAME);
    }

    /**
     * {@inheritDoc}
     * Executes the "update" method by updating an entity using the Bibernate session.
     */
    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        log.trace(HANDLE_METHOD, method.getName());
        if (parameters.length > 0) {
            var sessionFactory = BibernateSessionFactoryContextHolder.getBibernateSessionFactory();
            try (var bringSession = sessionFactory.openSession()) {
                var entityClass = (Class<?>) repositoryDetails.entityType();
                bringSession.update(entityClass, parameters[0]);
                return Void.TYPE;
            }
        }

        log.warn(UPDATE_METHOD_SHOULD_HAVE_ONE_PARAMETER_ID);
        throw new MissingRequiredParametersInMethod(UPDATE_METHOD_SHOULD_HAVE_ONE_PARAMETER_ID);
    }
}
