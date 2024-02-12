package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown when there is an issue creating a class limitation.
 * Extends {@link BibernateGeneralException}.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class ClassLimitationCreationException extends BibernateGeneralException {
    /**
     * Constructs a new ClassLimitationCreationException with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     */
    public ClassLimitationCreationException(String message) {
        super(message);
    }
}
