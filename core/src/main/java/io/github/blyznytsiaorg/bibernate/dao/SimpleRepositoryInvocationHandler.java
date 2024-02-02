package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.dao.method.handler.SimpleRepositoryFactory;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * InvocationHandler implementation for dynamically handling method invocations on repository interfaces.
 * It delegates the invocation to a SimpleRepositoryFactory for handling repository-specific operations.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
public class SimpleRepositoryInvocationHandler implements InvocationHandler {
    /**
     * The SimpleRepositoryFactory used for handling repository-specific operations.
     */
    private final SimpleRepositoryFactory simpleRepositoryFactory;
    /**
     * Constructs a SimpleRepositoryInvocationHandler with a new instance of SimpleRepositoryFactory.
     */
    public SimpleRepositoryInvocationHandler() {
        this.simpleRepositoryFactory = new SimpleRepositoryFactory();
    }

    /**
     * Handles the method invocation by delegating it to the associated SimpleRepositoryFactory.
     *
     * @param proxy      The proxy object.
     * @param method     The method being invoked.
     * @param parameters The parameters for the method invocation.
     * @return The result of the method invocation.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] parameters) {
        return simpleRepositoryFactory.invoke(proxy, method, parameters);
    }

    /**
     * Registers a repository interface and creates a dynamic proxy for it.
     *
     * @param repositoryInterface The repository interface to be registered.
     * @param <T>                 The type of the repository interface.
     * @return A dynamic proxy instance for the registered repository interface.
     */
    public  <T> T registerRepository(Class<T> repositoryInterface) {
        simpleRepositoryFactory.registerRepository(repositoryInterface);

        return (T) Proxy.newProxyInstance(
                repositoryInterface.getClassLoader(),
                new Class<?>[]{repositoryInterface},
                new SimpleRepositoryInvocationHandler()
        );
    }
}