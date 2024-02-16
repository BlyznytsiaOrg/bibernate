package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate a failure in matching properties during
 * a comparison or matching operation in a program. This exception is typically
 * used when attempting to match properties, and the operation encounters an issue
 * preventing successful property matching.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class FailedToMatchPropertyException extends RuntimeException {

    /**
     * Constructs a new FailedToMatchPropertyException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public FailedToMatchPropertyException(String message) {
        super(message);
    }
}
