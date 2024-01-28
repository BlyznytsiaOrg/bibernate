package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.annotation.Query;
import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.dao.utils.HqlQueryInfo;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactoryContextHolder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
public class SimpleRepositoryMethodHqlQueryHandler implements SimpleRepositoryMethodHandler {
    @Override
    public boolean isMethodHandle(Method method) {
        return method.isAnnotationPresent(Query.class) &&
                method.getAnnotation(Query.class).hql() &&
                !method.getAnnotation(Query.class).nativeQuery();
    }

    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        var sessionFactory = BibernateSessionFactoryContextHolder.getBibernateSessionFactory();
        String hqlQuery = method.getAnnotation(Query.class).value();
        Class<?> entityType = (Class<?>) repositoryDetails.entityType();

        var hqlQueryInfo = new HqlQueryInfo(hqlQuery, entityType);
        String query = hqlQueryInfo.toNativeSql();

        try (var session = sessionFactory.openSession()){
            return session.findByQuery(entityType, query, parameters);
        }
    }
}
