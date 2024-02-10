package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.session.BibernateSession;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

import static io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder.getBibernateSessionFactory;

/**
 * Implementation of {@link SimpleRepositoryMethodHandler} for handling the "save" method.
 * This handler saves an entity using the Bibernate session.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Slf4j
public class SimpleRepositoryMethodSaveHandler implements SimpleRepositoryMethodHandler {

    /**
     * The prefix for the handled method (save).
     */
    private static final String METHOD_NAME = "save";
    private static final String DELETE_METHOD_SHOULD_HAVE_ONE_PARAMETER_ID =
            "Save method should have one parameter entity";

    /**
     * {@inheritDoc}
     * Checks if the method name is "save".
     */
    @Override
    public boolean isMethodHandle(Method method) {
        return method.getName().equals(METHOD_NAME);
    }

    /**
     * {@inheritDoc}
     * Executes the "save" method by saving an entity using the Bibernate session.
     */
    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        var methodName = method.getName();
        log.trace(HANDLE_METHOD, methodName);
        if (parameters.length > 0) {
            try (var bibernateSession = getBibernateSessionFactory().openSession()) {
                var entityClass = (Class<?>) repositoryDetails.entityType();
                return executeHelper(entityClass, parameters[0], bibernateSession);
            }
        }

        log.warn(DELETE_METHOD_SHOULD_HAVE_ONE_PARAMETER_ID);
        return null;
    }

    private <T> T executeHelper(Class<T> entityClass, Object entity, BibernateSession bringSession) {
        return bringSession.save(entityClass, entityClass.cast(entity));
    }
}
