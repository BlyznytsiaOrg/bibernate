package io.github.blyznytsiaorg.bibernate.exception;

public class BibernateSessionClosedException extends RuntimeException {
    
    public BibernateSessionClosedException() {
        super("Session is closed");
    }
}
