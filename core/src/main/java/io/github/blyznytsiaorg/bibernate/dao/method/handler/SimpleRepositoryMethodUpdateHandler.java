package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactoryContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Collections;

import static java.util.Objects.nonNull;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class SimpleRepositoryMethodUpdateHandler implements SimpleRepositoryMethodHandler {

    private static final String UPDATE = "update";
    private static final String HANDLE_GENERIC_METHOD = "Handle generic method {}";
    private static final String NOT_SUPPORTED_RETURN_TYPE_FOR_METHOD_NAME =
            "Not supported returnType: '%s' for methodName '%s'";

    @Override
    public boolean isMethodHandle(Method method) {
        return method.getName().startsWith(UPDATE);
    }

    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        log.trace(HANDLE_GENERIC_METHOD, method.getName());

        var sessionFactory = BibernateSessionFactoryContextHolder.getBibernateSessionFactory();
        var returnType = methodMetadata.getReturnType();
        if (nonNull(returnType.getEntityClass()) && parameters.length > 0) {
            try (var bringSession = sessionFactory.openSession()) {
                var entityClass = (Class<?>) repositoryDetails.entityType();
                return bringSession.update(entityClass, parameters[0]);
            }
        }

        throw new BibernateGeneralException(
                NOT_SUPPORTED_RETURN_TYPE_FOR_METHOD_NAME.formatted(returnType, method.getName()));
    }
}
