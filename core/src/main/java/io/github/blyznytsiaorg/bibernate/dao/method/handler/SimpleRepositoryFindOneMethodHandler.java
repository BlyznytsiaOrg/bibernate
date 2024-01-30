package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.exception.EntityNotFoundException;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactoryContextHolder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@AllArgsConstructor
@Slf4j
public class SimpleRepositoryFindOneMethodHandler implements SimpleRepositoryMethodHandler {

    private static final String METHOD_NAME = "findOne";
    private static final String HANDLE_METHOD = "Handle method {}";
    private static final String LOOKS_LIKE_S_WITHOUT_REQUIRED_PARAMETER_ID = "Looks like %s  without required parameter ID";
    private static final String ENTITY_NOT_FOUND = "Entity %s not found by ID %s";

    @Override
    public boolean isMethodHandle(Method method) {
        return method.getName().equals(METHOD_NAME);
    }

    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        var methodName = method.getName();
        log.trace(HANDLE_METHOD, methodName);
        if (parameters != null && parameters.length == 1) {
            var sessionFactory = BibernateSessionFactoryContextHolder.getBibernateSessionFactory();
            try (var bringSession = sessionFactory.openSession()) {
                var entityType = (Class<?>) repositoryDetails.entityType();
                var primaryKey = parameters[0];

                return bringSession.findById(entityType, primaryKey)
                        .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND.formatted(entityType.getSimpleName(), primaryKey)));
            }
        } else {
            throw new IllegalArgumentException(LOOKS_LIKE_S_WITHOUT_REQUIRED_PARAMETER_ID.formatted(methodName));
        }
    }
}
