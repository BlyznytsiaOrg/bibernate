package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.BibernateSessionFactory;
import io.github.blyznytsiaorg.bibernate.dao.BibernateRepository;
import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.dao.method.ReturnType;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.blyznytsiaorg.bibernate.dao.utils.RepositoryParserUtils.getParameterNames;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Slf4j
public class SimpleRepositoryFactory {
    private static final String CUSTOM_REPOSITORY_SHOULD_HAVE_ONE_CONSTRUCTOR_WITH_BIBERNATE_SESSION_FACTORY_MESSAGE
            = "Custom repository should have one constructor with bibernateSessionFactory message %s";

    private static final Map<String, RepositoryDetails> REPOSITORY_MEDATA = new HashMap<>();
    private static final List<Object> CUSTOM_REPOSITORY_IMPLEMENTATIONS = new ArrayList<>();

    private static final String CALL_METHOD_NAME_PARAMETERS_PARAMETER_NAMES_EXTENDS_INTERFACES =
            "Call {} methodName {} parameters {} parameterNames {} extends interfaces {}";
    private static final String NOT_SUPPORTED_INTERFACE_FOR_REPOSITORY_WITH_METHOD =
            "Not supported interface for repository with method %s";
    private static final String RESOLVE_REPOSITORY_DETAILS = "Found methodName on repository {} repositoryDetails {}";
    private static final String IMPLEMENTATION_FOR_METHOD_S_NOT_RESOLVED = "Implementation for method %s  not resolved";
    private static final String LOOKS_LIKE_METHOD_METADATA_NOT_FOUND_FOR_METHOD =
            "Looks like methodMetadata not found for {} method";
    private final BibernateSessionFactory sessionFactory;
    private final List<SimpleRepositoryMethodHandler> simpleRepositoryMethodHandlers;

    public SimpleRepositoryFactory(BibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.simpleRepositoryMethodHandlers = new ArrayList<>();
        simpleRepositoryMethodHandlers.add(new SimpleRepositoryFindByIdMethodHandler(sessionFactory));
        simpleRepositoryMethodHandlers.add(new SimpleRepositoryMethodFindByHandler(sessionFactory));
        simpleRepositoryMethodHandlers.add(new SimpleRepositoryMethodCustomImplHandler(CUSTOM_REPOSITORY_IMPLEMENTATIONS));
    }

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
                        .filter(handler -> handler.isMethodHandle(methodName))
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

    private Object createRepositoryInstance(Class<?> impl) {
        try {
            Constructor<?> constructor = impl.getConstructor(BibernateSessionFactory.class);
            return constructor.newInstance(sessionFactory);
        } catch (Exception exe) {
            throw new BibernateGeneralException(
                    CUSTOM_REPOSITORY_SHOULD_HAVE_ONE_CONSTRUCTOR_WITH_BIBERNATE_SESSION_FACTORY_MESSAGE.formatted(exe.getMessage()),
                    exe
            );
        }
    }

    private <T> void collectCustomRepositoriesImplementation(Class<T> repositoryInterface) {
        var reflections = new Reflections(repositoryInterface);
        Arrays.stream(repositoryInterface.getInterfaces())
                .filter(aClass -> !aClass.isAssignableFrom(BibernateRepository.class))
                .flatMap(aClass -> reflections.getSubTypesOf(aClass).stream())
                .filter(impl -> !impl.isInterface())
                .map(this::createRepositoryInstance)
                .forEach(CUSTOM_REPOSITORY_IMPLEMENTATIONS::add);
    }

    private <T> Map<String, MethodMetadata> getMethodMetadataMap(Class<T> repositoryInterface) {
        return Arrays.stream(repositoryInterface.getMethods())
                .collect(Collectors.toMap(Method::getName, this::getMethodMetadata));
    }

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

    private MethodMetadata getMethodMetadata(Method method, Type genericReturnType) {
        var returnType = genericReturnType instanceof ParameterizedType
                ? new ReturnType((ParameterizedType) genericReturnType)
                : new ReturnType(method.getReturnType());
        return new MethodMetadata(method.getName(), returnType);
    }
}
