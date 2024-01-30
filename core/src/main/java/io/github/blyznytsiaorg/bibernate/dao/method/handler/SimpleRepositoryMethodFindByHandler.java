package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.exception.EntityNotFoundException;
import io.github.blyznytsiaorg.bibernate.exception.NonUniqueResultException;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactoryContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.dao.utils.RepositoryParserUtils.buildQueryByMethodName;
import static java.util.Objects.nonNull;

/** *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class SimpleRepositoryMethodFindByHandler implements SimpleRepositoryMethodHandler {

    private static final String FIND_BY = "findBy";
    private static final String HANDLE_GENERIC_METHOD = "Handle generic method {}";
    private static final String NOT_SUPPORTED_RETURN_TYPE_FOR_METHOD_NAME =
            "Not supported returnType{} for methodName {}";
    private static final String EXPECTED_SINGLE_RESULT = "Expected single result but we have %s method %s";
    public static final String CANNOT_FIND_RESULT_FOR_S_IN_METHOD_S = "Cannot find result for %s in method %s parameters %s";

    @Override
    public boolean isMethodHandle(Method method) {
        return method.getName().startsWith(FIND_BY);
    }

    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        String methodName = method.getName();
        log.trace(HANDLE_GENERIC_METHOD, methodName);
        var whereQuery = buildQueryByMethodName(methodName);
        var returnType = methodMetadata.getReturnType();
        var sessionFactory = BibernateSessionFactoryContextHolder.getBibernateSessionFactory();

        if (nonNull(returnType.getGenericEntityClass()) && List.class.isAssignableFrom((Class<?>)returnType.getGenericEntityClass().getRawType())) {
            try (var bringSession = sessionFactory.openSession()) {
                var entityClass = (Class<?>) returnType.getGenericEntityClass().getActualTypeArguments()[0];
                return bringSession.findByWhere(entityClass, whereQuery, parameters);
            }
        } else if (nonNull(returnType.getEntityClass()) && parameters.length > 0) {
            try (var bringSession = sessionFactory.openSession()) {
                var entityClass  = (Class<?>) repositoryDetails.entityType();

                List<?> items = bringSession.findByWhere(entityClass, whereQuery, parameters);

                if (items.isEmpty()) {
                    throw new EntityNotFoundException(
                            CANNOT_FIND_RESULT_FOR_S_IN_METHOD_S.formatted(entityClass.getSimpleName(), methodName, Arrays.toString(parameters))
                    );
                }

                if (items.size() == 1) {
                    return items.get(0);
                }

                throw new NonUniqueResultException(EXPECTED_SINGLE_RESULT.formatted(items.size(), methodName));
            }
        } else if (nonNull(returnType.getGenericEntityClass()) && Optional.class.isAssignableFrom((Class<?>)returnType.getGenericEntityClass().getRawType())) {
            try (var bringSession = sessionFactory.openSession()) {
                var entityClass = (Class<?>) returnType.getGenericEntityClass().getActualTypeArguments()[0];

                List<?> items = bringSession.findByWhere(entityClass, whereQuery, parameters);

                if (items.isEmpty()) {
                    throw new EntityNotFoundException(
                            CANNOT_FIND_RESULT_FOR_S_IN_METHOD_S.formatted(entityClass.getSimpleName(), methodName, Arrays.toString(parameters))
                    );
                }

                if (items.size() == 1) {
                    Object entity = items.get(0);
                    if (returnType.getGenericEntityClass().getRawType().equals(Optional.class)) {
                        return Optional.ofNullable(entity);
                    }

                    return entity;
                }

                throw new NonUniqueResultException(EXPECTED_SINGLE_RESULT.formatted(items.size(), methodName));
            }
        }

        log.warn(NOT_SUPPORTED_RETURN_TYPE_FOR_METHOD_NAME, returnType, methodName);
        return Collections.emptyList();
    }
}
