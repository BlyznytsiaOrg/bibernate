package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

import java.util.Collection;

public interface Identity {

    <T> void saveWithIdentity(Class<T> entityClass, Collection<T> entity);
}
