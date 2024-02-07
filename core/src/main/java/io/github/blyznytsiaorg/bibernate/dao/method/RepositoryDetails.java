package io.github.blyznytsiaorg.bibernate.dao.method;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
/**
 * Represents details about a repository, including its name, primary key type, entity type,
 * implemented interfaces, and metadata for each repository method.
 *
 * @param repositoryName   The name of the repository.
 * @param primaryKeyType   The type of the primary key used by the repository.
 * @param entityType       The type of the entity managed by the repository.
 * @param interfaces       The list of interfaces implemented by the repository.
 * @param methodsMetadata  A map containing metadata for each repository method, keyed by method name.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public record RepositoryDetails(String repositoryName, Type primaryKeyType,
                                Type entityType, List<String> interfaces,
                                Map<String, MethodMetadata> methodsMetadata) {
}
