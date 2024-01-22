package io.github.blyznytsiaorg.bibernate.utils;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Objects;

@UtilityClass
public class CollectionUtils {
    
    public boolean isEmpty(Collection<?> collection) {
        return Objects.isNull(collection) || collection.isEmpty();
    }

    public boolean isNotEmpty(Collection<?> collection) {
        return Objects.nonNull(collection) && !collection.isEmpty();
    }
}
