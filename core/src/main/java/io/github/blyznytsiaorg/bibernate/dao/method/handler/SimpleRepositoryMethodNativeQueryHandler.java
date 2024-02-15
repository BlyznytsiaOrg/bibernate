package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.annotation.Query;
import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

import static io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder.getBibernateSessionFactory;

/**
 * Implementation of {@link SimpleRepositoryMethodHandler} for handling methods annotated with {@link Query}.
 * This handler executes native SQL queries.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
public class SimpleRepositoryMethodNativeQueryHandler implements SimpleRepositoryMethodHandler {
    /**
     * {@inheritDoc}
     * Checks if the method is annotated with {@link Query}, specifies a native query, and is not an HQL query.
     */
    @Override
    public boolean isMethodHandle(Method method) {
        return method.isAnnotationPresent(Query.class) &&
                method.getAnnotation(Query.class).nativeQuery() &&
                method.getAnnotation(Query.class).bql();
    }

    /**
     * {@inheritDoc}
     * Executes the native SQL query specified in the {@link Query} annotation.
     */
    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails, MethodMetadata methodMetadata) {
        log.trace(HANDLE_METHOD, method.getName());
        String query = method.getAnnotation(Query.class).value();

        try (var session = getBibernateSessionFactory().openSession()){
            return session.find(query, parameters);
        }
    }
}
