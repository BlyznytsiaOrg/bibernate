package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.BibernateRepository;
import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.dao.method.ReturnType;
import io.github.blyznytsiaorg.bibernate.session.BibernateContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.github.blyznytsiaorg.bibernate.dao.utils.RepositoryParserUtils.getParameterNames;
import static io.github.blyznytsiaorg.bibernate.utils.ClassCreatorUtils.createNewInstance;

/**
 * Factory class for handling dynamic proxy creation and method invocations on repository interfaces.
 * It registers repository details, collects custom repository implementations, and delegates method execution
 * to appropriate handlers based on the repository interface and method metadata.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
public class SimpleRepositoryFactory {
    /**
     * Message for indicating that a custom repository should have a default constructor.
     */
    private static final String CUSTOM_REPOSITORY_SHOULD_HAVE_DEFAULT_CONSTRUCTOR
            = "Custom repository %s should have default constructor message %s";
    /**
     * Message for indicating a call to a method with specific details.
     */
    private static final Map<String, RepositoryDetails> REPOSITORY_MEDATA = new HashMap<>();
    /**
     * Message for indicating that an interface is not supported for a repository method.
     */
    private static final List<Object> CUSTOM_REPOSITORY_IMPLEMENTATIONS = new ArrayList<>();
    /**
     * Message for indicating a call to a method with specific details.
     */
    private static final String CALL_METHOD_NAME_PARAMETERS_PARAMETER_NAMES_EXTENDS_INTERFACES =
            "Call {} methodName {} parameters {} parameterNames {} extends interfaces {}";
    /**
     * Message for indicating that an interface is not supported for a repository method.
     */
    private static final String NOT_SUPPORTED_INTERFACE_FOR_REPOSITORY_WITH_METHOD =
            "Not supported interface for repository with method %s";
    /**
     * Message for indicating the resolution of repository details for a method.
     */
    private static final String RESOLVE_REPOSITORY_DETAILS = "Found methodName on repository {} repositoryDetails {}";
    /**
     * Message for indicating that an implementation for a method is not resolved.
     */
    private static final String IMPLEMENTATION_FOR_METHOD_S_NOT_RESOLVED = "Implementation for method %s  not resolved";
    /**
     * Message for indicating that method metadata is not found for a method.
     */
    private static final String LOOKS_LIKE_METHOD_METADATA_NOT_FOUND_FOR_METHOD =
            "Looks like methodMetadata not found for {} method";
    /**
     * Message for indicating the creation of method handlers.
     */
    private static final String METHOD_HANDLER_CREATION = "Cannot create class %s message %s";
    private final List<SimpleRepositoryMethodHandler> simpleRepositoryMethodHandlers;

    /**
     * Constructs a SimpleRepositoryFactory and initializes the list of method handlers.
     */
    public SimpleRepositoryFactory() {
        var reflections = BibernateContextHolder.getReflections();

        simpleRepositoryMethodHandlers = reflections.getSubTypesOf(SimpleRepositoryMethodHandler.class)
                .stream()
                .filter(Predicate.not(aClass -> aClass.isAssignableFrom(SimpleRepositoryMethodCustomImplHandler.class)))
                .map(aClass -> aClass.cast(createNewInstance(aClass, METHOD_HANDLER_CREATION)))
                .collect(Collectors.toList());

        simpleRepositoryMethodHandlers.add(new SimpleRepositoryMethodCustomImplHandler(CUSTOM_REPOSITORY_IMPLEMENTATIONS));
    }

    /**
     * Registers a repository interface, collects custom repository implementations,
     * and initializes repository metadata information.
     *
     * @param repositoryInterface The repository interface to be registered.
     * @param <T>                 The type of the repository interface.
     */
    public  <T> void registerRepository(Class<T> repositoryInterface) {
        var genericInterface = repositoryInterface.getGenericInterfaces()[0];
        Type primaryKeyType = null;
        Type entityType = null;

        if (genericInterface instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            entityType = typeArguments[0];
            primaryKeyType = typeArguments[1];
        }

        if (!REPOSITORY_MEDATA.containsKey(repositoryInterface.getName())) {
            collectCustomRepositoriesImplementation(repositoryInterface);

            List<String> interfaces = Arrays.stream(repositoryInterface.getInterfaces()).map(Class::getName).toList();
            Map<String, MethodMetadata> methodsMetadata = getMethodMetadataMap(repositoryInterface);
            var repositoryDetails = new RepositoryDetails(
                    repositoryInterface.getName(), primaryKeyType, entityType, interfaces, methodsMetadata
            );
            REPOSITORY_MEDATA.put(repositoryInterface.getName(), repositoryDetails);
        }
    }

    /**
     * Invokes a method on the repository interface, delegates the execution to appropriate handlers,
     * and returns the result.
     *
     * @param proxy      The proxy object.
     * @param method     The method being invoked.
     * @param parameters The parameters for the method invocation.
     * @return The result of the method invocation.
     */
    public Object invoke(Object proxy, Method method, Object[] parameters) {
        List<String> parameterNames = getParameterNames(method);
        String methodName = method.getName();
        log.trace(CALL_METHOD_NAME_PARAMETERS_PARAMETER_NAMES_EXTENDS_INTERFACES,
                getClass().getSimpleName(), methodName, Arrays.toString(parameters),
                Arrays.toString(parameterNames.toArray()),
                Arrays.toString(proxy.getClass().getInterfaces())
        );

        var repositoryDetails = Arrays.stream(proxy.getClass().getInterfaces())
                .map(Class::getName)
                .map(REPOSITORY_MEDATA::get)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        NOT_SUPPORTED_INTERFACE_FOR_REPOSITORY_WITH_METHOD.formatted(methodName))
                );

        log.trace(RESOLVE_REPOSITORY_DETAILS, methodName, repositoryDetails);
        var methodMetadataMap = repositoryDetails.methodsMetadata();

        if (!methodMetadataMap.isEmpty()) {
            var methodMetadata = methodMetadataMap.get(methodName);

            if (Objects.nonNull(methodMetadata)) {
                return simpleRepositoryMethodHandlers.stream()
                        .filter(handler -> handler.isMethodHandle(method))
                        .findFirst()
                        .map(handler -> handler.execute(method, parameters, repositoryDetails, methodMetadata))
                        .orElseThrow(() -> {
                            log.trace(IMPLEMENTATION_FOR_METHOD_S_NOT_RESOLVED.formatted(methodName));
                            return new IllegalArgumentException(IMPLEMENTATION_FOR_METHOD_S_NOT_RESOLVED.formatted(methodName));
                        });
            } else {
                log.trace(LOOKS_LIKE_METHOD_METADATA_NOT_FOUND_FOR_METHOD, methodName);
            }
        }

        log.trace(IMPLEMENTATION_FOR_METHOD_S_NOT_RESOLVED.formatted(methodName));
        throw new IllegalArgumentException(IMPLEMENTATION_FOR_METHOD_S_NOT_RESOLVED.formatted(methodName));
    }

    /**
     * Collects implementations of custom repositories related to the provided repository interface
     * and adds them to the list of custom repository implementations.
     *
     * @param repositoryInterface The repository interface to collect implementations for.
     * @param <T>                 The type of the repository interface.
     */
    private <T> void collectCustomRepositoriesImplementation(Class<T> repositoryInterface) {
        var reflections = new Reflections(repositoryInterface);
        Arrays.stream(repositoryInterface.getInterfaces())
                .filter(aClass -> !aClass.isAssignableFrom(BibernateRepository.class))
                .flatMap(aClass -> reflections.getSubTypesOf(aClass).stream())
                .filter(impl -> !impl.isInterface())
                .map(aClass -> createNewInstance(aClass, CUSTOM_REPOSITORY_SHOULD_HAVE_DEFAULT_CONSTRUCTOR))
                .forEach(CUSTOM_REPOSITORY_IMPLEMENTATIONS::add);
    }

    /**
     * Retrieves method metadata for all methods in the provided repository interface.
     *
     * @param repositoryInterface The repository interface to extract method metadata from.
     * @param <T>                 The type of the repository interface.
     * @return A map containing method names as keys and corresponding MethodMetadata instances as values.
     */
    private <T> Map<String, MethodMetadata> getMethodMetadataMap(Class<T> repositoryInterface) {
        return Arrays.stream(repositoryInterface.getMethods())
                .collect(Collectors.toMap(Method::getName, this::getMethodMetadata));
    }

    /**
     * Retrieves method metadata, including return type and parameters, for the provided method.
     *
     * @param method The method to extract metadata from.
     * @return The MethodMetadata instance representing the metadata of the given method.
     */
    private MethodMetadata getMethodMetadata(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        MethodMetadata methodMetadata = getMethodMetadata(method, genericReturnType);

        for (int i = 0; i < method.getParameters().length; i++) {
            String parameterName = getParameterNames(method).get(i);
            String parameterType = method.getGenericParameterTypes()[i].getTypeName();
            methodMetadata.addParameter(parameterName, parameterType);
        }

        return methodMetadata;
    }

    /**
     * Creates a {@link MethodMetadata} instance based on the provided {@link Method} and its generic return type.
     *
     * @param method            The method for which metadata is being created.
     * @param genericReturnType The generic return type of the method.
     * @return A {@link MethodMetadata} instance representing the metadata of the given method.
     */
    private MethodMetadata getMethodMetadata(Method method, Type genericReturnType) {
        var returnType = genericReturnType instanceof ParameterizedType
                ? new ReturnType((ParameterizedType) genericReturnType)
                : new ReturnType(method.getReturnType());
        return new MethodMetadata(method.getName(), returnType);
    }
}
