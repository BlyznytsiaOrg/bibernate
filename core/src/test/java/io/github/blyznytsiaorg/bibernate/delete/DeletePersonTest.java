package io.github.blyznytsiaorg.bibernate.delete;


import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.simplerespository.Person;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.setupTables;
import static org.assertj.core.api.Assertions.assertThat;

class DeletePersonTest extends AbstractPostgresInfrastructurePrep {

    @Test
    @DisplayName("Should delete person by ID")
    void shouldDeletePersonById() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent("testdata.simplerespository");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                //when
                bibernateSession.deleteById(Person.class, 1L);

                //then
                var person = bibernateSession.findById(Person.class, 1L);

                assertThat(person).isEmpty();
                assertQueries(bibernateSessionFactory, List.of(
                        "DELETE FROM persons WHERE persons.id = ?;",
                        "SELECT * FROM persons WHERE id = ?;"));
            }
        }
    }

    @Test
    @DisplayName("Should delete person by entity")
    void shouldDeletePersonByEntity() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent("testdata.simplerespository");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var person = bibernateSession.findById(Person.class, 1L).orElseThrow();

                //when
                bibernateSession.delete(Person.class, person);

                //then
                var deletedPerson = bibernateSession.findById(Person.class, 1L);

                assertThat(deletedPerson).isEmpty();
                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT persons.id AS persons_id, persons.first_name AS persons_first_name, persons.last_name AS persons_last_name FROM persons WHERE persons.id = ?;",
                        "DELETE FROM persons WHERE persons.id = ?;",
                        "SELECT * FROM persons WHERE id = ?;"));
            }
        }
    }

    @Test
    @DisplayName("Should delete All person by ID")
    void shouldDeleteAllPersonById() {
        //given
        createTableWithData(3);
        var persistent = createPersistent("testdata.simplerespository");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                List<Person> persons = bibernateSession.findAll(Person.class);
                Collection<Object> ids = persons.stream().map(Person::getId).collect(Collectors.toList());

                //when
                bibernateSession.deleteAllById(Person.class, ids);

                //then
                List<Person> removedPersons = bibernateSession.findAll(Person.class);

                assertThat(removedPersons).isEmpty();
                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM persons;",
                        "DELETE FROM persons WHERE id = ?;",
                        "DELETE FROM persons WHERE id = ?;",
                        "DELETE FROM persons WHERE id = ?;",
                        "SELECT * FROM persons;"));
            }
        }
    }

    @Test
    @DisplayName("Should delete All person")
    void shouldDeleteAllPerson() {
        //given
        createTableWithData(3);
        var persistent = createPersistent("testdata.simplerespository");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                List<Person> persons = bibernateSession.findAll(Person.class);

                //when
                bibernateSession.deleteAll(Person.class, persons);

                //then
                List<Person> removedPersons = bibernateSession.findAll(Person.class);

                assertThat(removedPersons).isEmpty();
                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM persons;",
                        "DELETE FROM persons WHERE id = ?;",
                        "DELETE FROM persons WHERE id = ?;",
                        "DELETE FROM persons WHERE id = ?;",
                        "SELECT * FROM persons;"));
            }
        }
    }

    private void createTableWithData(int i) {
        setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_GENERAL_INSERT_STATEMENT.formatted("John" + i, "Doe" + i));
        setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_GENERAL_INSERT_STATEMENT.formatted("Jane" + i, "Smith" + i));
        setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_GENERAL_INSERT_STATEMENT.formatted("John" + i, "Smith" + i));
    }
}
