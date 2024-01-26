package io.github.blyznytsiaorg.bibernate.dao.exception;

import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public class EntityStateWasChangeException extends BibernateGeneralException {

    public EntityStateWasChangeException(String message) {
        super(message);
    }
}
