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

/**
 * Handler for executing the findBy methods in a simple repository.
 * Implements the {@link SimpleRepositoryMethodHandler} interface.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
public class SimpleRepositoryMethodFindByHandler implements SimpleRepositoryMethodHandler {
    /**
     * The prefix for the handled method (findBy).
     */
    private static final String METHOD_NAME = "findBy";

    /**
     * Checks if the given method is a findBy method.
     *
     * @param method The method to check.
     * @return {@code true} if the method is a findBy method, {@code false} otherwise.
     */
    @Override
    public boolean isMethodHandle(Method method) {
        return method.getName().startsWith(METHOD_NAME);
    }

    /**
     * Executes the findBy method in the simple repository.
     *
     * @param method           The findBy method to execute.
     * @param parameters       The parameters for the findBy method invocation.
     * @param repositoryDetails Details about the repository, including its name, primary key type, entity type, etc.
     * @param methodMetadata   Metadata for the repository method, including its name, return type, and parameters.
     * @return The result of the findBy method execution, a list of entities, a single entity, or an empty list
     *         based on the return type and method invocation.
     */
    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        String methodName = method.getName();
        log.trace(HANDLE_METHOD, methodName);
        var whereQuery = buildQueryByMethodName(methodName);
        var returnType = methodMetadata.getReturnType();
        var sessionFactory = BibernateSessionFactoryContextHolder.getBibernateSessionFactory();

        if (nonNull(returnType.getGenericEntityClass()) &&
                List.class.isAssignableFrom((Class<?>)returnType.getGenericEntityClass().getRawType())) {
            try (var bringSession = sessionFactory.openSession()) {
                var entityClass = (Class<?>) returnType.getGenericEntityClass().getActualTypeArguments()[0];
                return bringSession.findByWhere(entityClass, whereQuery, parameters);
            }
        } else if (nonNull(returnType.getEntityClass()) && parameters.length > 0) {
            try (var bringSession = sessionFactory.openSession()) {
                var entityClass  = (Class<?>) repositoryDetails.entityType();
                List<?> items = bringSession.findByWhere(entityClass, whereQuery, parameters);
                itemsIsEmpty(items, entityClass.getSimpleName(), methodName, parameters);

                if (items.size() == 1) {
                    return items.get(0);
                }

                throw new NonUniqueResultException(EXPECTED_SINGLE_RESULT.formatted(items.size(), methodName));
            }
        } else if (nonNull(returnType.getGenericEntityClass()) &&
                Optional.class.isAssignableFrom((Class<?>)returnType.getGenericEntityClass().getRawType())) {
            try (var bringSession = sessionFactory.openSession()) {
                var entityClass = (Class<?>) returnType.getGenericEntityClass().getActualTypeArguments()[0];
                List<?> items = bringSession.findByWhere(entityClass, whereQuery, parameters);
                itemsIsEmpty(items, entityClass.getSimpleName(), methodName, parameters);

                if (items.size() == 1) {
                    var entity = items.get(0);
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

    /**
     * Checks if the list of items is empty and throws an exception if no results are found.
     *
     * @param items      The list of items to check.
     * @param entityClass The name of the entity class.
     * @param methodName  The name of the method.
     * @param parameters  The parameters used in the method invocation.
     * @throws EntityNotFoundException if the list of items is empty.
     */
    private static void itemsIsEmpty(List<?> items, String entityClass, String methodName, Object[] parameters) {
        if (items.isEmpty()) {
            throw new EntityNotFoundException(
                    CANNOT_FIND_RESULT_FOR_S_IN_METHOD_S.formatted(entityClass, methodName, Arrays.toString(parameters))
            );
        }
    }
}
