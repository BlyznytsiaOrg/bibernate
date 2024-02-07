package io.github.blyznytsiaorg.bibernate.multirelation;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.multirelation.Author;
import testdata.multirelation.Course;
import testdata.multirelation.Note;
import testdata.multirelation.Person;

import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

class MultiRelationsTest extends AbstractPostgresInfrastructurePrep {
    
    @DisplayName("Should retrieve person and lazily courses and notes")
    @Test
    void shouldRetrievePersonAndRelations() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_COURSES_TABLES, CREATE_INSERT_PERSONS_COURSES_STATEMENTS);
        QueryUtils.setupTables(dataSource, CREATE_NOTES_TABLE, CREATE_INSERT_NOTES_STATEMENT);

        var persistent = createPersistent();
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var sessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = sessionFactory.openSession()) {
                // when
                Optional<Person> personOptional = session.findById(Person.class, 1L);

                // then
                assertThat(personOptional).isPresent();
                assertThat(personOptional.get().getId()).isEqualTo(1L);
                assertThat(personOptional.get().getFirstName()).isEqualTo("John");

                assertQueries(sessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));
                
                assertThat(personOptional.get().getCourses()).hasSize(2);

                assertQueries(sessionFactory, List.of(
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT courses.* FROM persons_courses " +
                                "INNER JOIN courses " +
                                "ON courses.id = persons_courses.course_id " +
                                "WHERE person_id = ?;",
                        "SELECT * FROM authors WHERE id = ?;"));
                
                List<Note> notes = personOptional.get().getNotes();

                assertThat(notes).hasSize(2);

                assertQueries(sessionFactory, List.of(
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT courses.* FROM persons_courses " +
                                "INNER JOIN courses " +
                                "ON courses.id = persons_courses.course_id " +
                                "WHERE person_id = ?;",
                        "SELECT * FROM authors WHERE id = ?;",
                        "SELECT * FROM notes WHERE person_id = ?;"));
            }
        }
    }
    
    @DisplayName("Should retrieve note and person")
    @Test
    void shouldRetrieveNoteAndPerson() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_COURSES_TABLES, CREATE_INSERT_PERSONS_COURSES_STATEMENTS);
        QueryUtils.setupTables(dataSource, CREATE_NOTES_TABLE, CREATE_INSERT_NOTES_STATEMENT);

        var persistent = createPersistent();
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var sessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = sessionFactory.openSession()) {
                // when
                Optional<Note> noteOptional = session.findById(Note.class, 1L);
                
                // then
                assertThat(noteOptional).isPresent();
                assertThat(noteOptional.get().getId()).isEqualTo(1L);
                assertThat(noteOptional.get().getText()).isEqualTo("My First Note");
                
                assertQueries(sessionFactory, List.of(
                        "SELECT * FROM notes WHERE id = ?;",
                        "SELECT * FROM persons WHERE id = ?;"));
                
                assertThat(noteOptional.get().getPerson()).isNotNull();
                assertThat(noteOptional.get().getPerson().getId()).isEqualTo(1L);

                assertQueries(sessionFactory, List.of(
                        "SELECT * FROM notes WHERE id = ?;",
                        "SELECT * FROM persons WHERE id = ?;"));
            }
        }
    }
    
    @DisplayName("Should retrieve course and author and lazily persons")
    @Test
    void shouldRetrieveCourseAndRelations() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_COURSES_TABLES, CREATE_INSERT_PERSONS_COURSES_STATEMENTS);
        QueryUtils.setupTables(dataSource, CREATE_NOTES_TABLE, CREATE_INSERT_NOTES_STATEMENT);

        var persistent = createPersistent();
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var sessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = sessionFactory.openSession()) {
                // when
                Optional<Course> courseOptional = session.findById(Course.class, 1L);

                // then
                assertThat(courseOptional).isPresent();
                assertThat(courseOptional.get().getId()).isEqualTo(1L);
                assertThat(courseOptional.get().getName()).isEqualTo("Bobocode 2.0");

                assertQueries(sessionFactory, List.of(
                        "SELECT * FROM courses WHERE id = ?;",
                        "SELECT * FROM authors WHERE id = ?;"));

                assertThat(courseOptional.get().getAuthor()).isNotNull();
                assertThat(courseOptional.get().getAuthor()).isNotNull();
                
                assertThat(courseOptional.get().getPersons()).hasSize(2);

                assertQueries(sessionFactory, List.of(
                        "SELECT * FROM courses WHERE id = ?;",
                        "SELECT * FROM authors WHERE id = ?;",
                        "SELECT persons.* FROM persons_courses " +
                                "INNER JOIN persons " +
                                "ON persons.id = persons_courses.person_id " +
                                "WHERE course_id = ?;"));
            }
        }
    }
    
    @DisplayName("Should retrieve author and lazily courses")
    @Test
    void shouldRetrieveCourseAndAuthor() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_COURSES_TABLES, CREATE_INSERT_PERSONS_COURSES_STATEMENTS);
        QueryUtils.setupTables(dataSource, CREATE_NOTES_TABLE, CREATE_INSERT_NOTES_STATEMENT);

        var persistent = createPersistent();
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var sessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = sessionFactory.openSession()) {
                // when
                Optional<Author> authorOptional = session.findById(Author.class, 1L);

                // then
                assertThat(authorOptional).isPresent();
                assertThat(authorOptional.get().getId()).isEqualTo(1L);
                assertThat(authorOptional.get().getName()).isEqualTo("Bobocode");

                assertQueries(sessionFactory, List.of("SELECT * FROM authors WHERE id = ?;"));

                assertThat(authorOptional.get().getCourses()).hasSize(2);

                assertQueries(sessionFactory, List.of(
                        "SELECT * FROM authors WHERE id = ?;",
                        "SELECT * FROM courses WHERE author_id = ?;"));
            }
        }
    }
}
