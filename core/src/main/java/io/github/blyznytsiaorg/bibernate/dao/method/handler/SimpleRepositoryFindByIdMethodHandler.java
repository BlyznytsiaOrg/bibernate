package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactoryContextHolder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Optional;

import static java.util.Objects.nonNull;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@AllArgsConstructor
@Slf4j
public class SimpleRepositoryFindByIdMethodHandler implements SimpleRepositoryMethodHandler {

    private static final String FIND_BY_ID = "findById";
    private static final String HANDLE_METHOD = "Handle method {}";
    private static final String CANNOT_GET_ENTITY_TYPE_FOR_METHOD = "Cannot get entityType for method %s";
    private static final String LOOKS_LIKE_S_WITHOUT_REQUIRED_PARAMETER_ID = "Looks like %s  without required parameter ID";

    @Override
    public boolean isMethodHandle(Method method) {
        return method.getName().equals(FIND_BY_ID);
    }

    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        return handleMethodFindById(method, parameters, repositoryDetails, methodMetadata);
    }

    private Object handleMethodFindById(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                                        MethodMetadata methodMetadata) {
        var sessionFactory = BibernateSessionFactoryContextHolder.getBibernateSessionFactory();
        String methodName = method.getName();
        log.trace(HANDLE_METHOD, methodName);
        if (parameters != null && parameters.length == 1) {
            if (repositoryDetails.entityType() instanceof Class<?> entityClass) {
                try (var bringSession = sessionFactory.openSession()) {
                    var returnType = methodMetadata.getReturnType();
                    Object primaryKey = parameters[0];

                    if (nonNull(returnType.getEntityClass())) {
                        return bringSession.findById(entityClass, primaryKey);
                    } else if (Optional.class.isAssignableFrom((Class<?>)returnType.getGenericEntityClass().getRawType())) {
                        return Optional.ofNullable(bringSession.findById(entityClass, primaryKey));
                    } else {
                        throw new IllegalArgumentException("Cannot return " + returnType.getGenericEntityClass() +
                                " should be Optional<" + repositoryDetails.entityType() +
                                "> or " + repositoryDetails.entityType());
                    }
                }
            } else {
                throw new IllegalArgumentException(CANNOT_GET_ENTITY_TYPE_FOR_METHOD.formatted(methodName));
            }
        } else {
            throw new IllegalArgumentException(LOOKS_LIKE_S_WITHOUT_REQUIRED_PARAMETER_ID.formatted(methodName));
        }
    }
}
