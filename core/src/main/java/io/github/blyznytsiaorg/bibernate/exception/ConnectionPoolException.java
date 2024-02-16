package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate an issue related to the connection pool in a program.
 * This exception is typically used when there is a problem with acquiring or managing
 * connections from a connection pool, and it extends the RuntimeException class for
 * unchecked exception handling.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class ConnectionPoolException extends RuntimeException {

    /**
     * Constructs a new ConnectionPoolException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public ConnectionPoolException(String message, Throwable cause) {
        super(message, cause);
    }
}
