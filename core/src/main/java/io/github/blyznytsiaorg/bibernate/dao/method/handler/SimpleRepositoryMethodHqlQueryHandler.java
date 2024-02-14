package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.annotation.Query;
import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.dao.utils.HqlQueryInfo;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

import static io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder.getBibernateSessionFactory;

/**
 * Implementation of {@link SimpleRepositoryMethodHandler} for handling methods annotated with {@link Query}.
 * This handler executes HQL (Hibernate Query Language) queries.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
public class SimpleRepositoryMethodHqlQueryHandler implements SimpleRepositoryMethodHandler {
    /**
     * {@inheritDoc}
     * Checks if the method is annotated with {@link Query}, specifies HQL, and is not a native query.
     */
    @Override
    public boolean isMethodHandle(Method method) {
        return method.isAnnotationPresent(Query.class) &&
                method.getAnnotation(Query.class).bql() &&
                !method.getAnnotation(Query.class).nativeQuery();
    }

    /**
     * {@inheritDoc}
     * Executes the HQL query specified in the {@link Query} annotation.
     */
    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        log.trace(HANDLE_METHOD, method.getName());
        String hqlQuery = method.getAnnotation(Query.class).value();
        Class<?> entityType = (Class<?>) repositoryDetails.entityType();

        var hqlQueryInfo = new HqlQueryInfo(hqlQuery, entityType);
        String query = hqlQueryInfo.toNativeSql();

        try (var session = getBibernateSessionFactory().openSession()){
            return session.findByQuery(entityType, query, parameters);
        }
    }
}
