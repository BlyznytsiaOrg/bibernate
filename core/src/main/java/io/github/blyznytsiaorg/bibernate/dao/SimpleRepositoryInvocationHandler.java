package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.BibernateSessionFactory;
import io.github.blyznytsiaorg.bibernate.dao.method.handler.SimpleRepositoryFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class SimpleRepositoryInvocationHandler implements InvocationHandler {
    private final BibernateSessionFactory sessionFactory;
    private final SimpleRepositoryFactory simpleRepositoryFactory;

    public SimpleRepositoryInvocationHandler(BibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.simpleRepositoryFactory = new SimpleRepositoryFactory(sessionFactory);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] parameters) {
        return simpleRepositoryFactory.invoke(proxy, method, parameters);
    }

    public  <T> T registerRepository(Class<T> repositoryInterface) {
        simpleRepositoryFactory.registerRepository(repositoryInterface);

        return (T) Proxy.newProxyInstance(
                repositoryInterface.getClassLoader(),
                new Class<?>[]{repositoryInterface},
                new SimpleRepositoryInvocationHandler(sessionFactory)
        );
    }
}