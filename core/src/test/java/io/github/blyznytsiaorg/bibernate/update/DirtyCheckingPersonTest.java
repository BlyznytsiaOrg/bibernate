package io.github.blyznytsiaorg.bibernate.update;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.findbyid.Person;
import testdata.update.PersonWithoutDynamicUpdate;

import java.util.List;
import java.util.UUID;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

class DirtyCheckingPersonTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should update person first name only due to DynamicUpdate annotation")
    @Test
    void shouldUpdatePersonFirstName() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_INSERT_STATEMENT);

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            String uuid = UUID.randomUUID().toString();
            String firstName;
            String lastName;

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                Person person = bibernateSession.findById(Person.class, 1L).orElseThrow();
                firstName = person.getFirstName();
                lastName = person.getLastName();
                person.setFirstName(person.getFirstName() + uuid);
            }

            //then
            assertQueries(bibernateSessionFactory, List.of(
                    "SELECT * FROM persons WHERE id=?;",
                    "UPDATE persons SET first_name = ? WHERE id=?;")
            );

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                Person person = bibernateSession.findById(Person.class, 1L).orElseThrow();

                //then
                assertThat(person.getFirstName()).isEqualTo(firstName + uuid);
                assertThat(person.getLastName()).isEqualTo(lastName);
            }

            //then
            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE id=?;"));
        }
    }

    @DisplayName("Should update person first name and last name only due to DynamicUpdate annotation")
    @Test
    void shouldUpdatePersonFirstNameAndLastName() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_INSERT_STATEMENT);

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            String uuid = UUID.randomUUID().toString();
            String firstName;
            String lastName;

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                Person person = bibernateSession.findById(Person.class, 1L).orElseThrow();
                firstName = person.getFirstName();
                lastName = person.getLastName();
                person.setFirstName(person.getFirstName() + uuid);
                person.setLastName(person.getLastName() + uuid);
            }

            //then
            assertQueries(bibernateSessionFactory, List.of(
                    "SELECT * FROM persons WHERE id=?;",
                    "UPDATE persons SET first_name = ?, last_name = ? WHERE id=?;")
            );


            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                Person person = bibernateSession.findById(Person.class, 1L).orElseThrow();

                //then
                assertThat(person.getFirstName()).isEqualTo(firstName + uuid);
                assertThat(person.getLastName()).isEqualTo(lastName + uuid);
            }

            //then
            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE id=?;"));
        }
    }

    @DisplayName("Should update all person first name and last name only due to DynamicUpdate annotation not present")
    @Test
    void shouldUpdatePersonFirstNameAndLastNameIfEntityNotHavDynamicUpdate() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_INSERT_STATEMENT);

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            String uuid = UUID.randomUUID().toString();
            String firstName;
            String lastName;

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                var person = bibernateSession.findById(PersonWithoutDynamicUpdate.class, 1L).orElseThrow();
                firstName = person.getFirstName();
                lastName = person.getLastName();
                person.setFirstName(person.getFirstName() + uuid);
            }

            //then
            assertQueries(bibernateSessionFactory, List.of(
                    "SELECT * FROM persons WHERE id=?;",
                    "UPDATE persons SET first_name = ?, last_name = ? WHERE id=?;")
            );

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                var person = bibernateSession.findById(PersonWithoutDynamicUpdate.class, 1L).orElseThrow();

                //then
                assertThat(person.getFirstName()).isEqualTo(firstName + uuid);
                assertThat(person.getLastName()).isEqualTo(lastName);
            }

            //then
            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE id=?;"));
        }
    }
}
