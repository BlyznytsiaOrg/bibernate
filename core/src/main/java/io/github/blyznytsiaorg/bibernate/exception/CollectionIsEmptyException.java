package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown when an operation or method expects a collection to contain elements,
 * but the provided collection is empty.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class CollectionIsEmptyException extends BibernateGeneralException {

    /**
     * Constructs a new CollectionIsEmptyException with the specified detail message.
     *
     * @param message the detail message.
     */
    public CollectionIsEmptyException(String message) {
        super(message);
    }
}
