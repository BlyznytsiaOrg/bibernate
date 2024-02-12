package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate a validation failure in the Bibernate framework.
 * This exception is typically used when data validation fails or when a business rule is violated.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class BibernateValidationException extends RuntimeException {

    /**
     * Constructs a new BibernateValidationException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public BibernateValidationException(String message) {
        super(message);
    }
}
