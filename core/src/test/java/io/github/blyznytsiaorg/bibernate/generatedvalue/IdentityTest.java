package io.github.blyznytsiaorg.bibernate.generatedvalue;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.generatedvalue.identity.Person;

public class IdentityTest extends AbstractPostgresInfrastructurePrep {

  @Test
  @DisplayName("Should get id from the serial column of the persons table")
  public void shouldGetIdFromSerialColumnAndSetItToEntity() {
    //given
    QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
    var persistent = createPersistent();

    try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
      var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
      try (var bibernateSession = bibernateSessionFactory.openSession()) {

        var person = new Person();
        person.setFirstName("John");
        person.setLastName("Smith");

        //when
        var savedPerson = bibernateSession.save(Person.class, person);

        //then
        assertThat(savedPerson).isNotNull();
        assertThat(savedPerson.getId()).isNotNull();
        assertThat(savedPerson.getId()).isEqualTo(2L);
      }

      //then
      assertQueries(bibernateSessionFactory, List.of(
          "INSERT INTO persons ( first_name, last_name ) VALUES ( ?, ? );"));
    }
  }
}
