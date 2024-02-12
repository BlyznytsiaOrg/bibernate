package io.github.blyznytsiaorg.bibernate.exception;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class BibernateValidationException extends RuntimeException {

    public BibernateValidationException(String message) {
        super(message);
    }

    public BibernateValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
