package io.github.blyznytsiaorg.bibernate.dao;

import io.github.blyznytsiaorg.bibernate.BibernateSessionFactory;
import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;
import io.github.blyznytsiaorg.bibernate.dao.method.ReturnType;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.reflect.*;
import java.util.*;

import static io.github.blyznytsiaorg.bibernate.dao.utils.RepositoryParserUtils.buildQueryByMethodName;
import static io.github.blyznytsiaorg.bibernate.dao.utils.RepositoryParserUtils.getParameterNames;
import static java.util.Objects.nonNull;

@Getter
@Slf4j
@RequiredArgsConstructor
public class SimpleRepositoryInvocationHandler implements InvocationHandler {
    private static final Map<String, RepositoryDetails> REPOSITORY_MEDATA = new HashMap<>();
    private static final List<Object> CUSTOM_REPOSITORY_IMPLEMENTATIONS = new ArrayList<>();

    private static final String FIND_BY = "findBy";
    private static final String FIND_BY_ID = "findById";
    public static final String CUSTOM_REPOSITORY_SHOULD_HAVE_ONE_CONSTRUCTOR_WITH_BIBERNATE_SESSION_FACTORY_MESSAGE
            = "Custom repository should have one constructor with bibernateSessionFactory message %s";
    public static final String CANNOT_GET_ENTITY_TYPE_FOR_METHOD = "Cannot get entityType for method %s";
    public static final String LOOKS_LIKE_S_WITHOUT_REQUIRED_PARAMETER_ID = "Looks like %s  without required parameter ID";
    public static final String NOT_SUPPORTED_INTERFACE_FOR_REPOSITORY_WITH_METHOD =
            "Not supported interface for repository with method %s";
    public static final String CALL_METHOD_NAME_PARAMETERS_PARAMETER_NAMES_EXTENDS_INTERFACES =
            "Call {} methodName {} parameters {} parameterNames {} extends interfaces {}";
    public static final String RESOLVE_REPOSITORY_DETAILS = "Found methodName on repository {} repositoryDetails {}";
    public static final String IMPLEMENTATION_FOR_METHOD_S_NOT_RESOLVED = "Implementation for method %s  not resolved";
    public static final String REGISTER_REPOSITORY = "Register repository {}";
    public static final String HANDLE_GENERIC_METHOD = "Handle generic method {}";
    public static final String HANDLE_METHOD = "Handle method {}";
    public static final String NOT_SUPPORTED_RETURN_TYPE_FOR_METHOD_NAME =
            "Not supported returnType{} for methodName {}";
    public static final String TRY_TO_FIND_OUT_CUSTOM_REPOSITORY_IMPLEMENTATION_FOR_S_METHOD =
            "Try to find out custom repository implementation for {} method";
    public static final String CLASS_METHOD_FOUND_WILL_INVOKE_IT_WITH_PARAMETERS =
            "Class {} Method {} found will invoke it with parameters {}";
    public static final String LOOKS_LIKE_METHOD_METADATA_NOT_FOUND_FOR_METHOD =
            "Looks like methodMetadata not found for {} method";

    private final BibernateSessionFactory sessionFactory;

    @Override
    public Object invoke(Object proxy, Method method, Object[] parameters)
            throws InvocationTargetException, IllegalAccessException {
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
                if (methodName.equals(FIND_BY_ID)) {
                    return handleMethodFindById(method, parameters, repositoryDetails, methodMetadata, methodName);
                } else if (methodName.startsWith(FIND_BY)) {
                    return handleMethodFindBy(parameters, methodName, methodMetadata);
                }
            } else {
                log.trace(LOOKS_LIKE_METHOD_METADATA_NOT_FOUND_FOR_METHOD, methodName);
            }


            log.trace(TRY_TO_FIND_OUT_CUSTOM_REPOSITORY_IMPLEMENTATION_FOR_S_METHOD, methodName);
            for (var customRepository : CUSTOM_REPOSITORY_IMPLEMENTATIONS) {
                for (var customRepositoryMethod : customRepository.getClass().getDeclaredMethods()) {
                    if (customRepositoryMethod.getName().equals(methodName)) {
                        log.trace(CLASS_METHOD_FOUND_WILL_INVOKE_IT_WITH_PARAMETERS,
                                customRepository.getClass().getSimpleName(), methodName, Arrays.toString(parameters));
                        return customRepositoryMethod.invoke(customRepository, parameters);
                    }
                }
            }
        }

        log.trace(IMPLEMENTATION_FOR_METHOD_S_NOT_RESOLVED.formatted(methodName));
        throw new IllegalArgumentException(IMPLEMENTATION_FOR_METHOD_S_NOT_RESOLVED.formatted(methodName));
    }

    public  <T> T registerRepository(Class<T> repositoryInterface) {
        log.trace(REGISTER_REPOSITORY, repositoryInterface.getName());
        var genericInterface = repositoryInterface.getGenericInterfaces()[0];
        Type primaryKeyType = null;
        Type entityType = null;

        if (genericInterface instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            primaryKeyType = typeArguments[0];
            entityType = typeArguments[1];
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

        return (T) Proxy.newProxyInstance(
                repositoryInterface.getClassLoader(),
                new Class<?>[]{repositoryInterface},
                new SimpleRepositoryInvocationHandler(sessionFactory)
        );
    }

    private <T> void collectCustomRepositoriesImplementation(Class<T> repositoryInterface) {
        var reflections = new Reflections(repositoryInterface);
        for (var aClass : repositoryInterface.getInterfaces()) {
            if (!aClass.isAssignableFrom(BibernateRepository.class)) {
                reflections.getSubTypesOf(aClass)
                        .stream()
                        .filter(impl -> !impl.isInterface())
                        .map(this::createRepositoryInstance)
                        .forEach(CUSTOM_REPOSITORY_IMPLEMENTATIONS::add);
            }
        }
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


    private List<?> handleMethodFindBy(Object[] parameters, String methodName, MethodMetadata methodMetadata) {
        log.trace(HANDLE_GENERIC_METHOD, methodName);
        var whereQuery = buildQueryByMethodName(methodName);
        var returnType = methodMetadata.getReturnType();
        if (nonNull(returnType.getGenericEntityClass()) && List.class.isAssignableFrom((Class<?>)returnType.getGenericEntityClass().getRawType())) {
            try (var bringSession = sessionFactory.openSession()) {
                var entityClass = (Class<?>) returnType.getGenericEntityClass().getActualTypeArguments()[0];
                return bringSession.findBy(entityClass, whereQuery, parameters);
            }
        } else {
            log.warn(NOT_SUPPORTED_RETURN_TYPE_FOR_METHOD_NAME, returnType, methodName);
        }

        return Collections.emptyList();
    }

    private Object handleMethodFindById(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                                        MethodMetadata methodMetadata, String methodName) {
        log.trace(HANDLE_METHOD, method.getName());
        if (parameters != null && parameters.length == 1) {
            if (repositoryDetails.entityType() instanceof Class<?> entityClass) {
                try (var bringSession = sessionFactory.openSession()) {
                    var returnType = methodMetadata.getReturnType();
                    Object primaryKey = parameters[0];

                    if (nonNull(returnType.getEntityClass())) {
                        return bringSession.findById(entityClass, primaryKey);
                    } else if (Optional.class.isAssignableFrom((Class<?>)returnType.getGenericEntityClass().getRawType())) {
                        return Optional.ofNullable(bringSession.findById(entityClass, primaryKey));
                    } else {
                        throw new IllegalArgumentException("Cannot return " + returnType.getGenericEntityClass() +
                                " should be Optional<" + repositoryDetails.entityType() +
                                "> or " + repositoryDetails.entityType());
                    }
                }
            } else {
                throw new IllegalArgumentException(CANNOT_GET_ENTITY_TYPE_FOR_METHOD.formatted(methodName));
            }
        } else {
            throw new IllegalArgumentException(LOOKS_LIKE_S_WITHOUT_REQUIRED_PARAMETER_ID.formatted(methodName));
        }
    }

    private <T> Map<String, MethodMetadata> getMethodMetadataMap(Class<T> repositoryInterface) {
        var methodsMetadata = new HashMap<String, MethodMetadata>();
        for (var method : repositoryInterface.getMethods()) {
            var genericReturnType = method.getGenericReturnType();
            var methodMetadata = getMethodMetadata(method, genericReturnType);
            Type[] parameterTypes = method.getGenericParameterTypes();

            for (int i = 0; i <  method.getParameters().length; i++) {
                String parameterName = getParameterNames(method).get(i);
                String parameterType = parameterTypes[i].getTypeName();
                methodMetadata.addParameter(parameterName, parameterType);
            }

            methodsMetadata.put(method.getName(), methodMetadata);
        }
        return methodsMetadata;
    }

    private MethodMetadata getMethodMetadata(Method method, Type genericReturnType) {
        MethodMetadata methodMetadata;

        if (ParameterizedType.class.isAssignableFrom(genericReturnType.getClass())) {
            methodMetadata = new MethodMetadata(method.getName(), new ReturnType((ParameterizedType) method.getGenericReturnType()));
        } else {
            methodMetadata = new MethodMetadata(method.getName(), new ReturnType(method.getReturnType()));
        }
        return methodMetadata;
    }
}