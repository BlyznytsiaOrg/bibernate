package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate that an operation or functionality in a program expects
 * the presence of a specific annotation, but the required annotation is missing.
 * This exception is typically used when a runtime check for a particular annotation fails.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class MissingAnnotationException extends RuntimeException {

    /**
     * Constructs a new MissingAnnotationException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public MissingAnnotationException(String message) {
        super(message);
    }
}
