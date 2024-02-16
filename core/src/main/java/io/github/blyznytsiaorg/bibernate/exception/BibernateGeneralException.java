package io.github.blyznytsiaorg.bibernate.exception;

/**
 * The base exception class for handling general runtime exceptions within a Bibernate application.
 * This exception is typically used to wrap and propagate various runtime issues that may occur
 * during the execution of Bibernate-related operations.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class BibernateGeneralException extends RuntimeException {

    /**
     * Constructs a new BibernateGeneralException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public BibernateGeneralException(String message) {
        super(message);
    }

    /**
     * Constructs a new BibernateGeneralException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public BibernateGeneralException(String message, Throwable cause) {
        super(message, cause);
    }
}
