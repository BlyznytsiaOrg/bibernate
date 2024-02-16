package io.github.blyznytsiaorg.bibernate.exception;

/**
 * Exception thrown to indicate that an unsupported or invalid data type has been encountered.
 * This exception is typically used when attempting to process an operation with a data type
 * that is not recognized or supported within the context.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
public class UnsupportedDataTypeException extends RuntimeException {

    /**
     * Constructs a new UnsupportedDataTypeException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public UnsupportedDataTypeException(String message) {
        super(message);
    }
}
