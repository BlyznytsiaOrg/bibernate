package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate an attempt to modify an immutable entity
 * in a Bibernate application. This exception is typically used when an
 * operation attempts to alter an entity that is intended to remain immutable.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class ImmutableEntityException extends BibernateGeneralException{

    /**
     * Constructs a new ImmutableEntityException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public ImmutableEntityException(String message) {
        super(message);
    }
}
