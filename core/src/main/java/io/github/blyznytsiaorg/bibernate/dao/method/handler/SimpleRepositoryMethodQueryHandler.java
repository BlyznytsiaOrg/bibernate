package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.annotation.Query;
import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactoryContextHolder;

import java.lang.reflect.Method;

public class SimpleRepositoryMethodQueryHandler implements SimpleRepositoryMethodHandler {
    @Override
    public boolean isMethodHandle(Method method) {
        return method.isAnnotationPresent(Query.class);
    }

    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails, MethodMetadata methodMetadata) {
        var sessionFactory = BibernateSessionFactoryContextHolder.getBibernateSessionFactory();
        String query = method.getAnnotation(Query.class).value();

        try (var session = sessionFactory.openSession()){
            return session.find(query, parameters);
        }
    }
}
