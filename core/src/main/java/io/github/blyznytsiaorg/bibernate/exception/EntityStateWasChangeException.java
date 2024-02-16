package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate that the state of an entity in a Bibernate application
 * was changed unexpectedly. This exception is typically used when an attempt to modify
 * the state of an entity contradicts the expected or allowed changes.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class EntityStateWasChangeException extends BibernateGeneralException {

    /**
     * Constructs a new EntityStateWasChangeException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public EntityStateWasChangeException(String message) {
        super(message);
    }
}
