package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static io.github.blyznytsiaorg.bibernate.dao.utils.RepositoryParserUtils.buildQueryByMethodName;
import static java.util.Objects.nonNull;

/**
 *
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

    private final BibernateSessionFactory sessionFactory;

    @Override
    public boolean isMethodHandle(String methodName) {
        return methodName.startsWith(FIND_BY);
    }

    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        return handleMethodFindBy(parameters, method.getName(), methodMetadata);
    }

    private List<?> handleMethodFindBy(Object[] parameters, String methodName, MethodMetadata methodMetadata) {
        log.trace(HANDLE_GENERIC_METHOD, methodName);
        var whereQuery = buildQueryByMethodName(methodName);
        var returnType = methodMetadata.getReturnType();
        if (nonNull(returnType.getGenericEntityClass()) && List.class.isAssignableFrom((Class<?>)returnType.getGenericEntityClass().getRawType())) {
            try (var bringSession = sessionFactory.openSession()) {
                var entityClass = (Class<?>) returnType.getGenericEntityClass().getActualTypeArguments()[0];
                return bringSession.findBy(entityClass, whereQuery, parameters);
            }
        } else {
            log.warn(NOT_SUPPORTED_RETURN_TYPE_FOR_METHOD_NAME, returnType, methodName);
        }

        return Collections.emptyList();
    }
}
