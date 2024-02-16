package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate an issue related to the data source in a Bibernate application.
 * This exception is typically used when there are problems interacting with the underlying
 * data source, and it extends the RuntimeException class for unchecked exception handling.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class BibernateDataSourceException extends RuntimeException {

    /**
     * Constructs a new BibernateDataSourceException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public BibernateDataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
