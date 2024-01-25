package io.github.blyznytsiaorg.bibernate.onetomany;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.onetomany.unidirectional.Note;
import testdata.onetomany.unidirectional.Person;

import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OneToManyPersonNotesTest extends AbstractPostgresInfrastructurePrep {
    
    @DisplayName("Should retrieve person and notes only when requested")
    @Test
    void shouldFindExistingPersonWithNotes() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        QueryUtils.setupTables(dataSource, CREATE_NOTES_TABLE, CREATE_INSERT_NOTES_STATEMENT);
        
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

    @DisplayName("Should retrieve person that doesn't have notes")
    @Test
    void shouldFindExistingPersonWithoutNotes() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        QueryUtils.setupTables(dataSource, CREATE_NOTES_TABLE, CREATE_DELETE_NOTES_STATEMENT);

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

                assertThat(notes).isEmpty();

                assertQueries(sessionFactory, List.of(
                  "SELECT * FROM persons WHERE id = ?;",
                  "SELECT * FROM notes WHERE person_id = ?;"));
            }
        }
    }
    
    @DisplayName("Should throw exception if @OneToMany annotation applied not to a Collection")
    @Test
    void shouldThrowExceptionIfAnnotationUsedIncorrectly() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        QueryUtils.setupTables(dataSource, CREATE_NOTES_TABLE, CREATE_DELETE_NOTES_STATEMENT);

        try (var entityManager = persistent.createBibernateEntityManager(); 
             var session = entityManager.getBibernateSessionFactory().openSession()) {
                // when
                // then
                assertThrows(BibernateGeneralException.class, 
                        () -> session.findById(testdata.onetomany.unidirectional.error.Person.class, 1L));
        }
    }
    
}
