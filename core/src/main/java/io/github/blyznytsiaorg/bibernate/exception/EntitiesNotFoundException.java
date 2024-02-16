package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate that a collection of entities could not be found in a program.
 * This exception is typically used when attempting to retrieve or operate on a set of entities
 * that are expected to exist, but are not found in the underlying data source.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class EntitiesNotFoundException extends RuntimeException {

    /**
     * Constructs a new EntitiesNotFoundException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public EntitiesNotFoundException(String message) {
        super(message);
    }
}
