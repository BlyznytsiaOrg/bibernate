package io.github.blyznytsiaorg.bibernate.onetomany;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.onetomany.bidirectional.Note;
import testdata.onetomany.bidirectional.Person;

class OneToManyBidirectionalTest extends AbstractPostgresInfrastructurePrep {

  @DisplayName("Should retrieve person and notes only when requested")
  @Test
  void shouldFindExistingPersonWithNotes() {
    // given
    QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
    QueryUtils.setupTables(dataSource, CREATE_NOTES_TABLE, CREATE_INSERT_NOTES_STATEMENT);

    var persistent = createPersistent();
    try (var entityManager = persistent.createBibernateEntityManager()) {
      var sessionFactory = entityManager.getBibernateSessionFactory();
      try (var session = sessionFactory.openSession()) {
        // when
        Optional<Person> personOptional = session.findById(Person.class, 1L);

        // then
        assertThat(personOptional).isPresent();
        assertThat(personOptional.get())
          .hasFieldOrPropertyWithValue("id", 1L)
          .hasFieldOrPropertyWithValue("firstName", "FirstName")
          .hasFieldOrPropertyWithValue("lastName", "LastName");

        assertQueries(sessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));

        List<Note> notes = personOptional.get().getNotes();

        assertThat(notes).hasSize(2);
        assertThat(notes.stream().map(Note::getText).toList())
          .contains("My First Note")
          .contains("My Second Note");

        assertQueries(sessionFactory, List.of(
          "SELECT * FROM persons WHERE id = ?;",
          "SELECT * FROM notes WHERE person_id = ?;"));
      }
    }
  }
  
}
