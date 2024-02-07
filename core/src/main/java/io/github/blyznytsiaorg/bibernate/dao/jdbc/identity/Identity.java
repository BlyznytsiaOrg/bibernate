package io.github.blyznytsiaorg.bibernate.dao.jdbc.identity;

public interface Identity {

  Object saveWithIdentity(Object entity);
}
