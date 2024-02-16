package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate that a method in a Bibernate application is missing
 * required parameters. This exception is typically used when attempting to invoke a
 * method without providing all the necessary parameters it requires.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class MissingRequiredParametersInMethod extends BibernateGeneralException{

    /**
     * Constructs a new MissingRequiredParametersInMethod with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public MissingRequiredParametersInMethod(String message) {
        super(message);
    }
}
