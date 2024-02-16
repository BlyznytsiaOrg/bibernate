package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate an error or issue related to object mapping
 * in a program. This exception is typically used when encountering problems
 * during the mapping process, such as incompatible types or missing mappings.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class MappingException extends RuntimeException {

    /**
     * Constructs a new MappingException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public MappingException(String message) {
        super(message);
    }
}
