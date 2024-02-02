package io.github.blyznytsiaorg.bibernate.manytomany;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.manytomany.bidirectional.Course;
import testdata.manytomany.bidirectional.Person;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManyToManyBidirectionalTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should retrieve existing person and Lazy list of courses")
    @Test
    void shouldRetrievePersonWithCourses() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_COURSES_TABLES, CREATE_INSERT_PERSONS_COURSES_STATEMENTS);

        var persistent = createPersistent();
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
                        .contains(Course.builder().id(1L).name("Bobocode 2.0").persons(Collections.emptyList()).build())
                        .contains(Course.builder().id(2L).name("Bobocode 3.0").persons(Collections.emptyList()).build());
                assertThat(courses2)
                        .hasSize(1)
                        .contains(Course.builder().id(1L).name("Bobocode 2.0").persons(Collections.emptyList()).build());
                assertThat(courses3)
                        .hasSize(1)
                        .contains(Course.builder().id(2L).name("Bobocode 3.0").persons(Collections.emptyList()).build());

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

    @DisplayName("Should retrieve existing course and Lazy list of persons")
    @Test
    void shouldRetrieveCourseWithPersons() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_COURSES_TABLES, CREATE_INSERT_PERSONS_COURSES_STATEMENTS);

        var persistent = createPersistent();
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var sessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = sessionFactory.openSession()) {
                // when
                Optional<Course> course1 = session.findById(Course.class, 1L);
                Optional<Course> course2 = session.findById(Course.class, 2L);

                // then
                assertThat(course1).isPresent();
                assertThat(course1.get())
                        .hasFieldOrPropertyWithValue("id", 1L)
                        .hasFieldOrPropertyWithValue("name", "Bobocode 2.0");
                assertThat(course2).isPresent();
                assertThat(course2.get())
                        .hasFieldOrPropertyWithValue("id", 2L)
                        .hasFieldOrPropertyWithValue("name", "Bobocode 3.0");;

                assertQueries(sessionFactory, List.of(
                        "SELECT * FROM courses WHERE id = ?;",
                        "SELECT * FROM courses WHERE id = ?;"));

                List<Person> persons1 = course1.get().getPersons();
                List<Person> persons2 = course2.get().getPersons();

                assertThat(persons1)
                        .hasSize(2)
                        .contains(Person.builder().id(1L).firstName("John").lastName("Doe").courses(Collections.emptyList()).build())
                        .contains(Person.builder().id(2L).firstName("Jordan").lastName("Rodriguez").courses(Collections.emptyList()).build());
                assertThat(persons2)
                        .hasSize(2)
                        .contains(Person.builder().id(1L).firstName("John").lastName("Doe").courses(Collections.emptyList()).build())
                        .contains(Person.builder().id(3L).firstName("Ava").lastName("Mitchell").courses(Collections.emptyList()).build());

                assertQueries(sessionFactory, List.of(
                        "SELECT * FROM courses WHERE id = ?;",
                        "SELECT * FROM courses WHERE id = ?;",
                        "SELECT persons.* FROM persons_courses " +
                                "INNER JOIN persons " +
                                "ON persons.id = persons_courses.person_id " +
                                "WHERE course_id = ?;",
                        "SELECT persons.* FROM persons_courses " +
                                "INNER JOIN persons " +
                                "ON persons.id = persons_courses.person_id " +
                                "WHERE course_id = ?;"));
            }
        }
    }

    @DisplayName("Should retrieve existing person and Lazy list of courses and then another Course")
    @Test
    void shouldRetrievePersonWithCoursesAndThenOtherCourse() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_COURSES_TABLES, CREATE_INSERT_PERSONS_COURSES_STATEMENTS);

        var persistent = createPersistent();
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var sessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = sessionFactory.openSession()) {
                // when
                Optional<Person> person1 = session.findById(Person.class, 1L);

                // then
                assertThat(person1).isPresent();
                assertThat(person1.get())
                        .hasFieldOrPropertyWithValue("id", 1L)
                        .hasFieldOrPropertyWithValue("firstName", "John")
                        .hasFieldOrPropertyWithValue("lastName", "Doe");

                assertQueries(sessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));

                List<Course> courses1 = person1.get().getCourses();

                assertThat(courses1).hasSize(2);

                assertQueries(sessionFactory, List.of(
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT courses.* FROM persons_courses " +
                                "INNER JOIN courses " +
                                "ON courses.id = persons_courses.course_id " +
                                "WHERE person_id = ?;"));

                Optional<Course> course2 = session.findById(Course.class, 2L); // should not be found in cache
                assertThat(course2).isPresent();
                assertThat(course2.get())
                        .hasFieldOrPropertyWithValue("id", 2L)
                        .hasFieldOrPropertyWithValue("name", "Bobocode 3.0");;
                
                List<Person> persons2 = course2.get().getPersons();
                assertThat(persons2)
                        .hasSize(2)
                        .contains(Person.builder().id(1L).firstName("John").lastName("Doe").courses(Collections.emptyList()).build())
                        .contains(Person.builder().id(3L).firstName("Ava").lastName("Mitchell").courses(Collections.emptyList()).build());

                assertQueries(sessionFactory, List.of(
                        "SELECT * FROM persons WHERE id = ?;",
                        "SELECT courses.* FROM persons_courses " +
                                "INNER JOIN courses ON courses.id = persons_courses.course_id WHERE person_id = ?;",
                        "SELECT * FROM courses WHERE id = ?;",
                        "SELECT persons.* FROM persons_courses " +
                                "INNER JOIN persons ON persons.id = persons_courses.person_id WHERE course_id = ?;"));
            }
        }
    }

    @DisplayName("Should throw exception if mappedBy missing")
    @Test
    void shouldThrowExceptionIfMappedByMissing() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_COURSES_TABLES, CREATE_INSERT_PERSONS_COURSES_STATEMENTS);

        var persistent = createPersistent();
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var sessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = sessionFactory.openSession()) {
                // when
                var courseOptional = session.findById(testdata.manytomany.bidirectional.error.Course.class, 1L);

                // then
                assertThat(courseOptional).isPresent();
                var course = courseOptional.get();
                assertThrows(BibernateGeneralException.class, () -> course.getPersons().size());
            }
        }
    }
}
