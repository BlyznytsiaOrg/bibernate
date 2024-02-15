package io.github.blyznytsiaorg.bibernate.manytomany;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.multirelation.Course;
import testdata.multirelation.Person;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;


class ManyToManySaveTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should save Person with courses")
    @Test
    void shouldSavePersonWithCourses() {
        var persistent = createPersistentWithBb2ddlCreate("testdata.multirelation");
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = bibernateSessionFactory.openSession()) {

                var course = new Course();
                course.setName("Course_name");

                Course savedCourse = session.save(Course.class, course);

                var person = new Person();
                person.getCourses().add(savedCourse);

                Person savedPerson = session.save(Person.class, person);
                session.flush();

                assertThat(savedPerson.getId()).isEqualTo(1L);
                assertThat(savedCourse.getId()).isEqualTo(1L);

                assertQueries(bibernateSessionFactory, List.of("INSERT INTO courses ( name ) VALUES ( ? );",
                "INSERT INTO persons ( first_name, last_name, person_address_id ) VALUES ( ?, ?, ? );",
                        "INSERT INTO persons_courses ( person_id, course_id ) VALUES ( ?, ? );"));

            }
        }
    }

}
