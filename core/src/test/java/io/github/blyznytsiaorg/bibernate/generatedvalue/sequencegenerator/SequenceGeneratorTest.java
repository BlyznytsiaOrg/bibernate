package io.github.blyznytsiaorg.bibernate.generatedvalue.sequencegenerator;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.generatedvalue.sequencegenerator.Person;

public class SequenceGeneratorTest extends AbstractPostgresInfrastructurePrep {

  @Test
  @DisplayName("Should generate id from proper sequence and set it to entity")
  public void shouldGenerateIdFromSequenceAndSetItToEntity() {
    //given
    QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
    QueryUtils.setupSequences(dataSource, CREATE_PERSON_ID_CUSTOM_SEQ);
    var persistent = createPersistent();

    try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
      var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
      try (var bibernateSession = bibernateSessionFactory.openSession()) {

        var person1 = new Person();
        person1.setFirstName("John");
        person1.setLastName("Smith");

        var person2 = new Person();
        person2.setFirstName("Jane");
        person2.setLastName("Smith");

        //when
        var savedPerson1 = bibernateSession.save(Person.class, person1);
        var savedPerson2 = bibernateSession.save(Person.class, person2);

        //then
        assertThat(savedPerson1).isNotNull();
        assertThat(savedPerson1.getId()).isNotNull();
        assertThat(savedPerson1.getId()).isEqualTo(2L);

        assertThat(savedPerson2).isNotNull();
        assertThat(savedPerson2.getId()).isNotNull();
        assertThat(savedPerson2.getId()).isEqualTo(3L);
      }

      //then
      assertQueries(bibernateSessionFactory, List.of(
          //"select next value for persons_id_seq;",
          "select nextval('person_id_custom_seq');",
          "INSERT INTO persons ( id, first_name, last_name ) VALUES ( ?, ?, ? );",
          "INSERT INTO persons ( id, first_name, last_name ) VALUES ( ?, ?, ? );"));
    }
  }
}
