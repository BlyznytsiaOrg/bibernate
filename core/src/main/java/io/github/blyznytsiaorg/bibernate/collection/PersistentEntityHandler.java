package io.github.blyznytsiaorg.bibernate.collection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public class PersistentEntityHandler<T> implements InvocationHandler {
    private final Supplier<T> objectSupplier;
    private T internalObject;

    public PersistentEntityHandler(Supplier<T> objectSupplier) {
        this.objectSupplier = objectSupplier;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (internalObject == null) {
            internalObject = objectSupplier.get();
        }

        return method.invoke(internalObject, args);
    }
}
