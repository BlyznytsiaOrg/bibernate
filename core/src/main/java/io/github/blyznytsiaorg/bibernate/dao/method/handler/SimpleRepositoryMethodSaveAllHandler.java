package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.session.BibernateSession;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Collection;

import static io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder.getBibernateSessionFactory;

/**
 * Implementation of {@link SimpleRepositoryMethodHandler} for handling the "saveAll" method.
 * This handler saves a list of entities using the Bibernate session.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@Slf4j
public class SimpleRepositoryMethodSaveAllHandler implements SimpleRepositoryMethodHandler {

    /**
     * The prefix for the handled method (saveAll).
     */
    private static final String METHOD_NAME = "saveAll";
    private static final String DELETE_METHOD_SHOULD_HAVE_ONE_PARAMETER_ID =
            "SaveAll method should have one parameter entities";

    /**
     * {@inheritDoc}
     * Checks if the method name is "saveAll".
     */
    @Override
    public boolean isMethodHandle(Method method) {
        return method.getName().equals(METHOD_NAME);
    }

    /**
     * {@inheritDoc}
     * Executes the "saveAll" method by saving a list of entities using the Bibernate session.
     */
    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        var methodName = method.getName();
        log.trace(HANDLE_METHOD, methodName);
        if (parameters.length > 0) {
            try (var bibernateSession = getBibernateSessionFactory().openSession()) {
                var entityClass = (Class<?>) repositoryDetails.entityType();
                var parameter = parameters[0];
                if (parameter instanceof Collection<?> entities) {
                    executeHelper(entityClass, entities, bibernateSession);
                }

                return Void.TYPE;
            }
        }

        log.warn(DELETE_METHOD_SHOULD_HAVE_ONE_PARAMETER_ID);
        return null;
    }

    /**
     * Helper method for executing the save operation on a BibernateSession instance.
     * Saves a collection of entities of the specified class using the provided BibernateSession.
     *
     * @param <T>          the type of the entities being saved
     * @param entityClass  the class of the entities to be saved
     * @param entities     the collection of entities to be saved
     * @param bringSession the BibernateSession instance used for saving entities
     */
    private <T> void executeHelper(Class<T> entityClass, Collection entities, BibernateSession bringSession) {
        bringSession.saveAll(entityClass, entities);
    }
}
