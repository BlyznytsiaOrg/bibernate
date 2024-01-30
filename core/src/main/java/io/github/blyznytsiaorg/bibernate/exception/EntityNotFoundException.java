package io.github.blyznytsiaorg.bibernate.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
