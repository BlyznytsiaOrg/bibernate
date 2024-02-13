package io.github.blyznytsiaorg.bibernate.exception;

public class ConnectionPoolException extends RuntimeException {
    public ConnectionPoolException(String message, Throwable cause) {
        super(message, cause);
    }
}
