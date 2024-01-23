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
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public class SimpleRepositoryMethodCustomImplHandler implements SimpleRepositoryMethodHandler {
    private static final String TRY_TO_FIND_OUT_CUSTOM_REPOSITORY_IMPLEMENTATION_FOR_S_METHOD =
            "Try to find out custom repository implementation for {} method";
    private static final String CLASS_METHOD_FOUND_WILL_INVOKE_IT_WITH_PARAMETERS =
            "Class {} Method {} found will invoke it with parameters {}";
    private static final String CANNOT_INVOKE_CLASS_METHOD_WITH_PARAMETERS_MESSAGE = "Cannot invoke class %s method %s " +
            "with parameters %s message %s";
    public static final String IMPLEMENTATION_FOR_METHOD_S_NOT_RESOLVED = "Implementation for method %s  not resolved";

    private final List<Object> customRepositoryImplementations;

    @Override
    public boolean isMethodHandle(String methodName) {
        return customRepositoryImplementations.stream()
                .flatMap(customRepositoryMethod -> Stream.of(customRepositoryMethod.getClass().getDeclaredMethods()))
                .map(Method::getName)
                .anyMatch(customRepositoryMethodName -> customRepositoryMethodName.equals(methodName));
    }

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
        }
        log.trace(IMPLEMENTATION_FOR_METHOD_S_NOT_RESOLVED.formatted(methodName));
        throw new IllegalArgumentException(IMPLEMENTATION_FOR_METHOD_S_NOT_RESOLVED.formatted(methodName));
    }
}
