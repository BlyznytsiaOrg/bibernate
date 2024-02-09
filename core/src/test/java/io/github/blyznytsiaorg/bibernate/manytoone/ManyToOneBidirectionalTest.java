package io.github.blyznytsiaorg.bibernate.manytoone;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.manytoone.bidirectional.Note;

class ManyToOneBidirectionalTest extends AbstractPostgresInfrastructurePrep {

  @DisplayName("Should retrieve note and person")
  @Test
  void shouldFindExistingNoteWithPerson() {
    // given
    QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
    QueryUtils.setupTables(dataSource, CREATE_NOTES_TABLE, CREATE_INSERT_NOTES_STATEMENT);

    var persistent = createPersistent();
    try (var entityManager = persistent.createBibernateEntityManager()) {
      var sessionFactory = entityManager.getBibernateSessionFactory();
      try (var session = sessionFactory.openSession()) {
        // when
        Optional<Note> noteOptional = session.findById(Note.class, 1L);

        // then
        assertThat(noteOptional).isPresent();
        assertThat(noteOptional.get())
          .hasFieldOrPropertyWithValue("id", 1L)
          .hasFieldOrPropertyWithValue("text", "My First Note");

        assertQueries(sessionFactory, List.of(
          "SELECT * FROM notes WHERE id = ?;",
          "SELECT * FROM persons WHERE id = ?;"));

        assertThat(noteOptional.get().getPerson())
          .hasFieldOrPropertyWithValue("id", 1L)
          .hasFieldOrPropertyWithValue("firstName", "FirstName")
          .hasFieldOrPropertyWithValue("lastName", "LastName");

        assertQueries(sessionFactory, List.of(
          "SELECT * FROM notes WHERE id = ?;",
          "SELECT * FROM persons WHERE id = ?;"));
      }
    }
  }
  
}
