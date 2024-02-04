package io.github.blyznytsiaorg.bibernate.exception;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class BibernateSessionClosedException extends RuntimeException {
    
    public BibernateSessionClosedException(String message) {
        super(message);
    }
}
