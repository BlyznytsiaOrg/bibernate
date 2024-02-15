package io.github.blyznytsiaorg.bibernate.utils;

import io.github.blyznytsiaorg.bibernate.exception.CollectionIsEmptyException;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Objects;

/**
 * Utility class for common operations on collections.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@UtilityClass
public class CollectionUtils {

    /**
     * Checks if a collection is empty.
     *
     * @param collection the collection to check
     * @return true if the collection is null or empty, false otherwise
     */
    public boolean isEmpty(Collection<?> collection) {
        return Objects.isNull(collection) || collection.isEmpty();
    }

    /**
     * Checks if a collection is not empty.
     *
     * @param collection the collection to check
     * @return true if the collection is not null and not empty, false otherwise
     */
    public boolean isNotEmpty(Collection<?> collection) {
        return Objects.nonNull(collection) && !collection.isEmpty();
    }

    /**
     * Requires that a collection is not empty, throwing an exception with the specified message if it is.
     *
     * @param collection the collection to check
     * @param message    the message to include in the exception if the collection is empty
     * @throws CollectionIsEmptyException if the collection is empty
     */
    public void requireNonEmpty(Collection<?> collection, String message) {
        if (isEmpty(collection)) {
            throw new CollectionIsEmptyException(message);
        }
    }
}
