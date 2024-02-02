package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

public interface Identity {

  Object saveWithIdentity(Object entity);
}
