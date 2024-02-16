package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate an attempt to perform an operation on a closed session
 * in a Bibernate application. This exception is typically used when an operation requires
 * an open session, but the session is already closed.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class BibernateSessionClosedException extends RuntimeException {

    /**
     * Constructs a new BibernateSessionClosedException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public BibernateSessionClosedException(String message) {
        super(message);
    }
}
