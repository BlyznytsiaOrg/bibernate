package io.github.blyznytsiaorg.bibernate.cascade.remove;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.cascade.remove.onetomany.bidirectional.Note;
import testdata.cascade.remove.onetomany.bidirectional.Person;

import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CascadeRemoveTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should remove cascade OneToMany bidirectional")
    @Test
    void shouldRemoveParentAndChildEntities_oneToMany() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        QueryUtils.setupTables(dataSource, CREATE_NOTES_TABLE, CREATE_INSERT_NOTES_STATEMENT);

        var persistent = createPersistent("testdata.cascade.remove.onetomany.bidirectional");
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = bibernateSessionFactory.openSession()) {
                // when
                // then
                Optional<Person> personOptional = session.findById(Person.class, 1L);

                assertThat(personOptional).isPresent();
                assertThat(personOptional.get().getNotes()).hasSize(2);

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT * FROM notes WHERE person_id = ?;"));

                session.deleteById(Person.class, 1L);

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT * FROM notes WHERE person_id = ?;",
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT * FROM notes WHERE person_id = ?;",
                        "DELETE FROM notes WHERE person_id = ?;",
                        "DELETE FROM persons WHERE id = ?;"));

                personOptional = session.findById(Person.class, 1L);
                Optional<Note> noteOptional1 = session.findById(Note.class, 1L);
                Optional<Note> noteOptional2 = session.findById(Note.class, 2L);

                assertThat(personOptional).isEmpty();
                assertThat(noteOptional1).isEmpty();
                assertThat(noteOptional2).isEmpty();
            }
        }
    }

    @DisplayName("Should remove cascade OneToMany unidirectional")
    @Test
    void shouldRemoveParentAndChildEntities_oneToMany_unidirectional() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        QueryUtils.setupTables(dataSource, CREATE_NOTES_TABLE, CREATE_INSERT_NOTES_STATEMENT);

        var persistent = createPersistent("testdata.cascade.remove.onetomany.bidirectional");
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = bibernateSessionFactory.openSession()) {
                // when
                // then
                var personOptional = session.findById(Person.class, 1L);

                assertThat(personOptional).isPresent();
                assertThat(personOptional.get().getNotes()).hasSize(2);

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT * FROM notes WHERE person_id = ?;"));

                session.deleteById(Person.class, 1L);

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT * FROM notes WHERE person_id = ?;",
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT * FROM notes WHERE person_id = ?;",
                        "DELETE FROM notes WHERE person_id = ?;",
                        "DELETE FROM persons WHERE id = ?;"));

                personOptional = session.findById(Person.class, 1L);
                var noteOptional1 = session.findById(Note.class, 1L);
                var noteOptional2 = session.findById(Note.class, 2L);

                assertThat(personOptional).isEmpty();
                assertThat(noteOptional1).isEmpty();
                assertThat(noteOptional2).isEmpty();
            }
        }
    }

    @DisplayName("Should remove cascade ManyToOne bidirectional")
    @Test
    void shouldRemoveParentIfChildRemoved_manyToOne() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        QueryUtils.setupTables(dataSource, CREATE_NOTES_TABLE, CREATE_DELETE_NOTES_STATEMENT);
        QueryUtils.setupTables(dataSource, CREATE_NOTES_TABLE, CREATE_INSERT_NOTE_STATEMENT);

        var persistent = createPersistent("testdata.cascade.remove.manytoone.bidirectional");
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = bibernateSessionFactory.openSession()) {
                // when
                // then
                var noteOptional = session.findById(testdata.cascade.remove.manytoone.bidirectional.Note.class, 1L);

                assertThat(noteOptional).isPresent();
                assertThat(noteOptional.get().getPerson()).isNotNull();

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM notes WHERE id = ?;",
                        "SELECT * FROM persons WHERE id = ?;"));

                session.deleteById(testdata.cascade.remove.manytoone.bidirectional.Note.class, 1L);

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM notes WHERE id = ?;",
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT * FROM notes WHERE id = ?;",
                        "DELETE FROM notes WHERE id = ?;",
                        "DELETE FROM persons WHERE id = ?;"));

                noteOptional = session.findById(testdata.cascade.remove.manytoone.bidirectional.Note.class, 1L);
                var personOptional = session.findById(testdata.cascade.remove.manytoone.bidirectional.Person.class, 1L);

                assertThat(noteOptional).isEmpty();
                assertThat(personOptional).isEmpty();
            }
        }
    }

    @DisplayName("Should not remove cascade ManyToOne unidirectional. Person with many Notes.")
    @Test
    void shouldNotRemoveParentWithChildren_manyToOne_unidirectional() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        QueryUtils.setupTables(dataSource, CREATE_NOTES_TABLE, CREATE_INSERT_NOTES_STATEMENT);

        var persistent = createPersistent("testdata.cascade.remove.manytoone.unidirectional");
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = bibernateSessionFactory.openSession()) {
                // when
                // then
                var noteOptional = session.findById(testdata.cascade.remove.manytoone.unidirectional.Note.class, 1L);

                assertThat(noteOptional).isPresent();
                assertThat(noteOptional.get().getPerson()).isNotNull();

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM notes WHERE id = ?;",
                        "SELECT * FROM persons WHERE id = ?;"));

                session.deleteById(testdata.cascade.remove.manytoone.unidirectional.Note.class, 1L);
                
                noteOptional = session.findById(testdata.cascade.remove.manytoone.unidirectional.Note.class, 1L);
                var personOptional = session.findById(testdata.cascade.remove.manytoone.unidirectional.Person.class, 1L);

                assertThat(noteOptional).isEmpty();
                assertThat(personOptional).isPresent();
            }
        }
    }
}
