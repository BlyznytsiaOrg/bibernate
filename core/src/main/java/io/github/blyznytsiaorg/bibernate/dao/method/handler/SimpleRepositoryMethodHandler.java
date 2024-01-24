package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;

import java.lang.reflect.Method;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public interface SimpleRepositoryMethodHandler {

    boolean isMethodHandle(Method method);

    Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                   MethodMetadata methodMetadata);
}
