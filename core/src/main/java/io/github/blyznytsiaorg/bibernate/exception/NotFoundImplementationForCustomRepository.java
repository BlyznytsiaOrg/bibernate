package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate that no implementation is found for a custom repository
 * in a Bibernate application. This exception is typically used when attempting to use
 * a custom repository for which no implementation is available or configured.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class NotFoundImplementationForCustomRepository extends BibernateGeneralException{

    /**
     * Constructs a new NotFoundImplementationForCustomRepository with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public NotFoundImplementationForCustomRepository(String message) {
        super(message);
    }
}
