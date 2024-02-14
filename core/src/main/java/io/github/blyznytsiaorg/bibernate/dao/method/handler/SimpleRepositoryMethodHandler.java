package io.github.blyznytsiaorg.bibernate.dao.method.handler;

import io.github.blyznytsiaorg.bibernate.dao.method.MethodMetadata;
import io.github.blyznytsiaorg.bibernate.dao.method.RepositoryDetails;

import java.lang.reflect.Method;

/**
 * Interface for handling the execution of repository methods. Implementations of this interface
 * define how to identify and execute specific repository methods.
 *
 * <p>Example:</p>
 * Suppose we have a repository method {@code findById} that retrieves an entity by its primary key. An implementation
 * of this interface could identify this method and execute it by delegating to the appropriate data access layer
 * for retrieving the entity from the database.
 *
 * <pre>{@code
 * public class UserRepositoryMethodHandler implements SimpleRepositoryMethodHandler {
 *
 *     // Implementation of isMethodHandle method
 *     public boolean isMethodHandle(Method method) {
 *         return method.getName().equals("findById");
 *     }
 *
 *     // Implementation of execute method
 *     public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
 *                           MethodMetadata methodMetadata) {
 *         // Implementation to retrieve entity by primary key from database
 *     }
 * }
 * }</pre>
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public interface SimpleRepositoryMethodHandler {

    /**
     * Log message template for handling a method.
     */
    String HANDLE_METHOD = "Handle method {}";
    /**
     * Error message template for a method without the required parameter ID.
     */
    String LOOKS_LIKE_METHOD_WITHOUT_REQUIRED_PARAMETER_ID =
            "Looks like method %s  without required parameter ID";
    /**
     * Error message template for an invalid return type. It should be Optional or Type.
     */
    String CANNOT_RETURN_S_SHOULD_BE_OPTIONAL_S_OR_S = "Cannot return %s should be Optional<%s> or %s";
    /**
     * Error message template for an unsupported return type for a method.
     */
     String NOT_SUPPORTED_RETURN_TYPE_FOR_METHOD_NAME = "Not supported returnType {} for methodName {}";

    /**
     * Error message template for an expected single result but found multiple in method.
     */
    String EXPECTED_SINGLE_RESULT = "Expected single result but we have %s method %s";
    /**
     * Error message template for being unable to find a result for entityClass in method with parameters.
     */
    String CANNOT_FIND_RESULT_FOR_S_IN_METHOD_S = "Cannot find result for %s in method %s parameters %s";

    /**
     * Checks if the given method can be handled by this handler.
     *
     * @param method The method to check.
     * @return {@code true} if this handler can handle the method, {@code false} otherwise.
     */
    boolean isMethodHandle(Method method);

    /**
     * Executes the given repository method using the provided parameters, repository details, and method metadata.
     *
     * @param method            The repository method to execute.
     * @param parameters        The parameters for the method invocation.
     * @param repositoryDetails Details about the repository, including its name, primary key type, entity type, etc.
     * @param methodMetadata    Metadata for the repository method, including its name, return type, and parameters.
     * @return The result of the repository method execution.
     */
    Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                   MethodMetadata methodMetadata);
}
