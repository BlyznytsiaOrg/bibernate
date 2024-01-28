package io.github.blyznytsiaorg.bibernate.exception;

public class ImmutableEntityException extends BibernateGeneralException{
    public ImmutableEntityException(String message) {
        super(message);
    }
}
