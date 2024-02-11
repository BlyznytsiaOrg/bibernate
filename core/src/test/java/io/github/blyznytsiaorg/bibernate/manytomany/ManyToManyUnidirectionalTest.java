package io.github.blyznytsiaorg.bibernate.manytomany;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.exception.BibernateSessionClosedException;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.manytomany.unidirectional.positive.Course;
import testdata.manytomany.unidirectional.positive.Person;

import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManyToManyUnidirectionalTest extends AbstractPostgresInfrastructurePrep {
    
    @DisplayName("Should retrieve existing person and Lazy list of courses")
    @Test
    void shouldRetrievePersonWithCourses() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_COURSES_TABLES, CREATE_INSERT_PERSONS_COURSES_STATEMENTS);
        
        var persistent = createPersistent("testdata.manytomany.unidirectional.positive");
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var sessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = sessionFactory.openSession()) {
                // when
                Optional<Person> person1 = session.findById(Person.class, 1L);
                Optional<Person> person2 = session.findById(Person.class, 2L);
                Optional<Person> person3 = session.findById(Person.class, 3L);

                // then
                assertThat(person1).isPresent();
                assertThat(person1.get())
                        .hasFieldOrPropertyWithValue("id", 1L)
                        .hasFieldOrPropertyWithValue("firstName", "John")
                        .hasFieldOrPropertyWithValue("lastName", "Doe");
                assertThat(person2).isPresent();
                assertThat(person2.get())
                        .hasFieldOrPropertyWithValue("id", 2L)
                        .hasFieldOrPropertyWithValue("firstName", "Jordan")
                        .hasFieldOrPropertyWithValue("lastName", "Rodriguez");
                assertThat(person3).isPresent();
                assertThat(person3.get())
                        .hasFieldOrPropertyWithValue("id", 3L)
                        .hasFieldOrPropertyWithValue("firstName", "Ava")
                        .hasFieldOrPropertyWithValue("lastName", "Mitchell");
                
                assertQueries(sessionFactory, List.of(
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT * FROM persons WHERE id = ?;"));
                
                List<Course> courses1 = person1.get().getCourses();
                List<Course> courses2 = person2.get().getCourses();
                List<Course> courses3 = person3.get().getCourses();

                assertThat(courses1)
                        .hasSize(2)
                        .contains(Course.builder().id(1L).name("Bobocode 2.0").build())
                        .contains(Course.builder().id(2L).name("Bobocode 3.0").build());
                assertThat(courses2)
                        .hasSize(1)
                        .contains(Course.builder().id(1L).name("Bobocode 2.0").build());
                assertThat(courses3)
                        .hasSize(1)
                        .contains(Course.builder().id(2L).name("Bobocode 3.0").build());

                assertQueries(sessionFactory, List.of(
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT courses.* FROM persons_courses " +
                                "INNER JOIN courses " +
                                "ON courses.id = persons_courses.course_id " +
                                "WHERE person_id = ?;",
                        "SELECT courses.* FROM persons_courses " +
                                "INNER JOIN courses " +
                                "ON courses.id = persons_courses.course_id " +
                                "WHERE person_id = ?;",
                        "SELECT courses.* FROM persons_courses " +
                                "INNER JOIN courses " +
                                "ON courses.id = persons_courses.course_id " +
                                "WHERE person_id = ?;"));
            }
        }
    }

    @DisplayName("Should retrieve existing course without persons")
    @Test
    void shouldRetrieveCourseWithoutPersons() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_COURSES_TABLES, CREATE_INSERT_PERSONS_COURSES_STATEMENTS);

        var persistent = createPersistent("testdata.manytomany.unidirectional.positive");
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var sessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = sessionFactory.openSession()) {
                // when
                Optional<Course> course = session.findById(Course.class, 1L);

                // then
                assertThat(course).isPresent();
                assertThat(course.get())
                        .hasFieldOrPropertyWithValue("id", 1L)
                        .hasFieldOrPropertyWithValue("name", "Bobocode 2.0");

                assertQueries(sessionFactory, List.of("SELECT courses.id AS courses_id, courses.name AS courses_name FROM courses WHERE courses.id = ?;"));
            }
        }
    }

    @DisplayName("Should throw exception if getting Lazy list after session is closed")
    @Test
    void shouldThrowExceptionIfSessionIsClosed() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_COURSES_TABLES, CREATE_INSERT_PERSONS_COURSES_STATEMENTS);

        var persistent = createPersistent("testdata.manytomany.unidirectional.positive");
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var sessionFactory = entityManager.getBibernateSessionFactory();
            var session = sessionFactory.openSession();
                // when
                Optional<Person> person1 = session.findById(Person.class, 1L);
                Optional<Person> person2 = session.findById(Person.class, 2L);
                Optional<Person> person3 = session.findById(Person.class, 3L);

                // then
                assertThat(person1).isPresent();
                assertThat(person1.get())
                        .hasFieldOrPropertyWithValue("id", 1L)
                        .hasFieldOrPropertyWithValue("firstName", "John")
                        .hasFieldOrPropertyWithValue("lastName", "Doe");
                assertThat(person2).isPresent();
                assertThat(person2.get())
                        .hasFieldOrPropertyWithValue("id", 2L)
                        .hasFieldOrPropertyWithValue("firstName", "Jordan")
                        .hasFieldOrPropertyWithValue("lastName", "Rodriguez");
                assertThat(person3).isPresent();
                assertThat(person3.get())
                        .hasFieldOrPropertyWithValue("id", 3L)
                        .hasFieldOrPropertyWithValue("firstName", "Ava")
                        .hasFieldOrPropertyWithValue("lastName", "Mitchell");

                assertQueries(sessionFactory, List.of(
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT * FROM persons WHERE id = ?;"));

                session.close();
                
                Person person = person1.get();
                assertThrows(BibernateSessionClosedException.class, () -> person.getCourses().size());
        }
    }

    @DisplayName("Should throw exception if joinColumn names incorrect")
    @Test
    void shouldThrowExceptionIfJoinColumnIncorrect() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_COURSES_TABLES, CREATE_INSERT_PERSONS_COURSES_STATEMENTS);

        var persistent = createPersistent("testdata.manytomany.unidirectional.error");
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var sessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = sessionFactory.openSession()) {
                // when
                var personOptional = session.findById(testdata.manytomany.unidirectional.error.Person.class, 1L);
                
                assertThat(personOptional).isPresent();
                
                // then
                var person = personOptional.get();
                assertThrows(BibernateGeneralException.class, () -> person.getCourses().size());
            }
        }
    }
}
