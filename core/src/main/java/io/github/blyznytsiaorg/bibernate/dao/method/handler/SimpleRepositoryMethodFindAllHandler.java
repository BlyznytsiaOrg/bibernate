package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactoryContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.nonNull;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class SimpleRepositoryMethodFindAllHandler implements SimpleRepositoryMethodHandler {

    private static final String FIND_ALL = "findAll";
    private static final String HANDLE_GENERIC_METHOD = "Handle generic method {}";
    private static final String NOT_SUPPORTED_RETURN_TYPE_FOR_METHOD_NAME =
            "Not supported returnType{} for methodName {}";

    @Override
    public boolean isMethodHandle(Method method) {
        return method.getName().startsWith(FIND_ALL);
    }

    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        return handleMethodFindBy(parameters, method.getName(), repositoryDetails, methodMetadata);
    }

    private Object handleMethodFindBy(Object[] parameters, String methodName, RepositoryDetails repositoryDetails, MethodMetadata methodMetadata) {
        log.trace(HANDLE_GENERIC_METHOD, methodName);
        var sessionFactory = BibernateSessionFactoryContextHolder.getBibernateSessionFactory();
        var returnType = methodMetadata.getReturnType();
        if (nonNull(returnType.getGenericEntityClass()) && List.class.isAssignableFrom((Class<?>)returnType.getGenericEntityClass().getRawType())) {
            try (var bringSession = sessionFactory.openSession()) {
                var entityClass  = (Class<?>) repositoryDetails.entityType();
                return bringSession.findBy(entityClass, null, parameters);
            }
        } else {
            log.warn(NOT_SUPPORTED_RETURN_TYPE_FOR_METHOD_NAME, returnType, methodName);
        }

        //TODO need to fix later when we will have findOne T findByUsername(@Param("username") String username)

        return Collections.emptyList();
    }
}
