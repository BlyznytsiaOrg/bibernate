package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate that an unsupported or invalid return type has been encountered
 * in the Bibernate framework. This exception is typically used when attempting to process an entity
 * action with a return type that is not recognized or supported.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class UnsupportedReturnTypeException extends BibernateGeneralException {

    /**
     * Constructs a new UnsupportedReturnTypeException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public UnsupportedReturnTypeException(String message) {
        super(message);
    }
}
