package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Handler for executing custom methods in a custom repository implementation.
 * Implements the {@link SimpleRepositoryMethodHandler} interface.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class SimpleRepositoryMethodCustomImplHandler implements SimpleRepositoryMethodHandler {
    /**
     * Message indicating an attempt to find a custom repository implementation for a specific method.
     */
    private static final String TRY_TO_FIND_OUT_CUSTOM_REPOSITORY_IMPLEMENTATION_FOR_S_METHOD =
            "Try to find out custom repository implementation for {} method";
    /**
     * Message indicating that a class method is found and will be invoked with specified parameters.
     */
    private static final String CLASS_METHOD_FOUND_WILL_INVOKE_IT_WITH_PARAMETERS =
            "Class {} Method {} found. Will invoke it with parameters {}";
    /**
     * Message indicating the failure to invoke a class method with specified parameters.
     */
    private static final String CANNOT_INVOKE_CLASS_METHOD_WITH_PARAMETERS_MESSAGE = "Cannot invoke class %s method %s " +
            "with parameters %s message %s";
    /**
     * Message indicating that the implementation for a specific method is not resolved.
     */
    public static final String IMPLEMENTATION_FOR_METHOD_S_NOT_RESOLVED = "Implementation for method %s  not resolved";

    /**
     * List of custom repository implementations.
     */
    private final List<Object> customRepositoryImplementations;

    /**
     * Checks if the given method is handled by any of the custom repository implementations.
     *
     * @param method The method to check.
     * @return {@code true} if the method is handled, {@code false} otherwise.
     */
    @Override
    public boolean isMethodHandle(Method method) {
        return customRepositoryImplementations.stream()
                .flatMap(customRepositoryMethod -> Stream.of(customRepositoryMethod.getClass().getDeclaredMethods()))
                .map(Method::getName)
                .anyMatch(customRepositoryMethodName -> customRepositoryMethodName.equals(method.getName()));
    }

    /**
     * Executes the custom method in the custom repository implementation.
     *
     * @param method           The method to execute.
     * @param parameters       The parameters for the method invocation.
     * @param repositoryDetails Details about the repository, including its name, primary key type, entity type, etc.
     * @param methodMetadata   Metadata for the repository method, including its name, return type, and parameters.
     * @return The result of the custom method execution.
     * @throws IllegalArgumentException If the custom method implementation for the given method is not found.
     */
    @Override
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        String methodName = method.getName();
        log.trace(TRY_TO_FIND_OUT_CUSTOM_REPOSITORY_IMPLEMENTATION_FOR_S_METHOD, methodName);
        for (var customRepository : customRepositoryImplementations) {
            for (var customRepositoryMethod : customRepository.getClass().getDeclaredMethods()) {
                if (customRepositoryMethod.getName().equals(methodName)) {
                    log.trace(CLASS_METHOD_FOUND_WILL_INVOKE_IT_WITH_PARAMETERS,
                            customRepository.getClass().getSimpleName(), methodName, Arrays.toString(parameters));
                    return invokeMethod(parameters, customRepository, customRepositoryMethod);
                }
            }
        }
        log.trace(IMPLEMENTATION_FOR_METHOD_S_NOT_RESOLVED.formatted(methodName));
        throw new IllegalArgumentException(IMPLEMENTATION_FOR_METHOD_S_NOT_RESOLVED.formatted(methodName));
    }

    /**
     * Invokes the specified method on the custom repository implementation with the given parameters.
     *
     * @param parameters           The parameters for the method invocation.
     * @param customRepository     The custom repository implementation instance.
     * @param customRepositoryMethod The method to invoke.
     * @return The result of the method invocation.
     * @throws BibernateGeneralException If an error occurs during method invocation.
     */
    private static Object invokeMethod(Object[] parameters, Object customRepository, Method customRepositoryMethod) {
        try {
            return customRepositoryMethod.invoke(customRepository, parameters);
        } catch (Exception exe) {
            var errorMessage = CANNOT_INVOKE_CLASS_METHOD_WITH_PARAMETERS_MESSAGE.formatted(
                    customRepository.getClass(),
                    customRepositoryMethod.getName(),
                    Arrays.toString(parameters), exe.getMessage());
            log.error(errorMessage, exe);
            throw new BibernateGeneralException(errorMessage, exe);
        }
    }
}
