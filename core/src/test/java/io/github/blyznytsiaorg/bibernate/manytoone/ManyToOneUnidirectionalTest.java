package io.github.blyznytsiaorg.bibernate.manytoone;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.manytoone.unidirectional.Note;

import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManyToOneUnidirectionalTest extends AbstractPostgresInfrastructurePrep {

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

    @DisplayName("Should throw exception if @ManyToOne annotation applied to a Collection")
    @Test
    void shouldThrowExceptionIfAnnotationUsedIncorrectly() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        QueryUtils.setupTables(dataSource, CREATE_NOTES_TABLE, CREATE_INSERT_NOTES_STATEMENT);

        var persistent = createPersistent();
        try (var entityManager = persistent.createBibernateEntityManager();
             var session = entityManager.getBibernateSessionFactory().openSession()) {
            // when
            // then
            assertThrows(BibernateGeneralException.class,
                    () -> session.findById(testdata.manytoone.unidirectional.badannotation.Note.class, 1L));
        }
    }

    @DisplayName("Should treat as regular field if not annotated with @ManyToOne")
    @Test
    void shouldRetrieveNoteWithoutPersonWhenNoAnnotation() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        QueryUtils.setupTables(dataSource, CREATE_NOTES_TABLE, CREATE_INSERT_NOTES_STATEMENT);

        var persistent = createPersistent();
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var sessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = sessionFactory.openSession()) {
                // when
                var noteOptional = session.findById(testdata.manytoone.unidirectional.notannotated.Note.class, 1L);

                // then
                assertThat(noteOptional).isPresent();
                assertThat(noteOptional.get())
                        .hasFieldOrPropertyWithValue("id", 1L)
                        .hasFieldOrPropertyWithValue("text", "My First Note")
                        .hasFieldOrPropertyWithValue("person", null);

                assertQueries(sessionFactory, List.of("SELECT * FROM notes WHERE id = ?;"));

                assertThat(noteOptional.get().getPerson()).isNull();

                assertQueries(sessionFactory, List.of("SELECT * FROM notes WHERE id = ?;"));
            }
        }
    }

    @DisplayName("Should retrieve person without relations")
    @Test
    void shouldFindExistingPersonWithoutNotesRelation() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        QueryUtils.setupTables(dataSource, CREATE_NOTES_TABLE, CREATE_DELETE_NOTES_STATEMENT);

        var persistent = createPersistent();
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var sessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = sessionFactory.openSession()) {
                // when
                var personOptional = session.findById(testdata.manytoone.unidirectional.notannotated.Person.class, 1L);

                // then
                assertThat(personOptional).isPresent();
                assertThat(personOptional.get())
                        .hasFieldOrPropertyWithValue("id", 1L)
                        .hasFieldOrPropertyWithValue("firstName", "FirstName")
                        .hasFieldOrPropertyWithValue("lastName", "LastName");

                assertQueries(sessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));
            }
        }
    }
    
}
