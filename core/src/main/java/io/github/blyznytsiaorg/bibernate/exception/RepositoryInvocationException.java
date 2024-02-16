package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate an issue related to repository method invocation in a Bibernate application.
 * This exception is typically used when an error occurs while invoking a method in the repository layer,
 * and it extends the more general exception, BibernateGeneralException, which captures broader exceptions
 * within the Bibernate framework.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class RepositoryInvocationException extends BibernateGeneralException{

    /**
     * Constructs a new RepositoryInvocationException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public RepositoryInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
