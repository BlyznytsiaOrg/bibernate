package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate that an entity could not be found in a program.
 * This exception is typically used when attempting to retrieve or operate on
 * an entity that is expected to exist, but is not found in the underlying data source.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class EntityNotFoundException extends RuntimeException {

    /**
     * Constructs a new EntityNotFoundException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
