package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate that an unsupported or invalid action type has been encountered
 * in the Bibernate framework. This exception is typically used when attempting to process an
 * entity action with an unrecognized or unsupported type.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class UnsupportedActionTypeException extends BibernateGeneralException {

    /**
     * Constructs a new UnsupportedActionTypeException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public UnsupportedActionTypeException(String message) {
        super(message);
    }
}
