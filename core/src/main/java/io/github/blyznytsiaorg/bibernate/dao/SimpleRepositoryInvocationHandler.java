package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.dao.method.handler.SimpleRepositoryFactory;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
public class SimpleRepositoryInvocationHandler implements InvocationHandler {
    private final SimpleRepositoryFactory simpleRepositoryFactory;
    public SimpleRepositoryInvocationHandler() {
        this.simpleRepositoryFactory = new SimpleRepositoryFactory();
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
                new SimpleRepositoryInvocationHandler()
        );
    }
}