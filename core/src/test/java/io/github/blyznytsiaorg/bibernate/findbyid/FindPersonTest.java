package io.github.blyznytsiaorg.bibernate.findbyid;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.findbyid.Person;

import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

class FindPersonTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should find exising person by ID")
    @Test
    void shouldFindExistingPersonById() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent("testdata.findbyid");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                Optional<Person> person = bibernateSession.findById(Person.class, 1L);

                //then
                assertThat(person).isPresent();
                assertThat(person.get().getFirstName()).isEqualTo("FirstName");
                assertThat(person.get().getLastName()).isEqualTo("LastName");
            }

            //then
            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));
        }
    }

    @DisplayName("Should return empty optional when person not found")
    @Test
    void shouldReturnEmptyOptionalWhenPersonNotFound() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent("testdata.findbyid");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                Optional<Person> person = bibernateSession.findById(Person.class, 2L);

                //then
                assertThat(person).isEmpty();
            }

            //then
            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));
        }
    }

    @DisplayName("Should return person from first level cache")
    @Test
    void shouldReturnPersonFromFirstLevelCache() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent("testdata.findbyid");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                Person person1 = bibernateSession.findById(Person.class, 1L).orElseThrow();
                Person person2 = bibernateSession.findById(Person.class, 1L).orElseThrow();

                //then
                assertThat(person1).isEqualTo(person2);
            }

            //then
            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));
        }
    }
}
