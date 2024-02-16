package io.github.blyznytsiaorg.bibernate.utils;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Utility class providing methods to create proxies.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@UtilityClass
public class ProxyUtils {

    /**
     * Creates a dynamic proxy for the specified class using the provided supplier.
     * This method generates a proxy instance that extends the specified class and delegates method invocations
     * to the provided supplier.
     *
     * @param clazz    the class for which a proxy should be created
     * @param supplier the supplier to delegate method invocations to object
     * @return a dynamic proxy instance for the specified class
     */
    @SneakyThrows
    public Object createProxy(Class<?> clazz, Supplier<?> supplier) {
        var proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(clazz);

        var methodHandler = new Handler(supplier);

        return proxyFactory.create(new Class<?>[0], new Object[0], methodHandler);
    }

    /**
     * Handler class implementing MethodHandler for proxy invocation.
     */
    @RequiredArgsConstructor
    public class Handler implements MethodHandler {
        private final Supplier<?> supplier;
        private Object internalObject;


        /**
         * Invokes the method on the proxy object and delegates to the actual object.
         *
         * @param self       The proxy object
         * @param thisMethod The method being invoked on the proxy
         * @param proceed    The proceed method
         * @param args       The arguments for the method invocation
         * @return The result of the method invocation on the actual object
         */
        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
            if (internalObject == null) {
                internalObject = supplier.get();
            }
            return thisMethod.invoke(internalObject, args);
        }
    }

}
