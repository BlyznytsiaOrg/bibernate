package io.github.blyznytsiaorg.bibernate.dao.method;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public record RepositoryDetails(String repositoryName, Type primaryKeyType,
                                Type entityType, List<String> interfaces,
                                Map<String, MethodMetadata> methodsMetadata) {
}
