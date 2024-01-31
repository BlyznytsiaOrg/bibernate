package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

public interface Identity {

  <T> Object saveWithIdentity(Object entity);
}
