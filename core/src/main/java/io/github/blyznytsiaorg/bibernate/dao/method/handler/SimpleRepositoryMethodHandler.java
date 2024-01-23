package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.BibernateSessionFactory;
import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;

import java.lang.reflect.Method;

public interface SimpleRepositoryMethodHandler {

    boolean isMethodHandle(String methodName);

    Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                   MethodMetadata methodMetadata, BibernateSessionFactory sessionFactory);
}
