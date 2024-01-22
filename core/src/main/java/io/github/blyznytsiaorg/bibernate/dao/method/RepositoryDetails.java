package io.github.blyznytsiaorg.bibernate.dao.method;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public record RepositoryDetails(String repositoryName, Type primaryKeyType,
                                Type entityType, List<String> interfaces,
                                Map<String, MethodMetadata> methodsMetadata) {
}
