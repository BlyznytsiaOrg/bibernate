package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactoryContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class SimpleRepositoryMethodDeleteAllHandler implements SimpleRepositoryMethodHandler {

    private static final String DELETE = "deleteAll";
    private static final String HANDLE_GENERIC_METHOD = "Handle generic method {}";
    private static final String DELETE_METHOD_SHOULD_HAVE_ONE_PARAMETER_ID =
            "DeleteAll method should have one parameter List<Id>";

    @Override
    public boolean isMethodHandle(Method method) {
        return method.getName().equals(DELETE);
    }

    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        var methodName = method.getName();
        log.trace(HANDLE_GENERIC_METHOD, methodName);
        var sessionFactory = BibernateSessionFactoryContextHolder.getBibernateSessionFactory();
        if (parameters.length > 0) {
            try (var bringSession = sessionFactory.openSession()) {
                var entityClass  = (Class<?>) repositoryDetails.entityType();
                Object parameter = parameters[0];
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
