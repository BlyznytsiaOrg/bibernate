package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate that a query or operation resulted in a non-unique result
 * in a runtime context. This exception is typically used when an expectation of a unique
 * result is violated, and multiple results are encountered.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class NonUniqueResultException extends RuntimeException {

    /**
     * Constructs a new NonUniqueResultException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public NonUniqueResultException(String message) {
        super(message);
    }
}
