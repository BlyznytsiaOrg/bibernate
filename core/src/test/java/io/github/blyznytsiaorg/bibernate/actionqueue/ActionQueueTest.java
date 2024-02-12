package io.github.blyznytsiaorg.bibernate.actionqueue;


import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.simplerespository.Person;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.setupTables;
import static org.assertj.core.api.Assertions.assertThat;

class ActionQueueTest extends AbstractPostgresInfrastructurePrep {

    @Test
    @DisplayName("Should call only delete query before flush")
    void shouldCallDeleteQuery() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent("testdata.simplerespository");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var person = new Person();
                person.setId(2L);
                person.setFirstName("Rake");
                person.setLastName("Tell");

                //when
                bibernateSession.save(Person.class, person);

                person.setFirstName("New Rake");
                bibernateSession.update(Person.class, person);

                bibernateSession.delete(Person.class, person);
                bibernateSession.flush();

                //then
                assertQueries(bibernateSessionFactory, List.of("DELETE FROM persons WHERE id = ?;"));
            }
        }
    }

    @Test
    @DisplayName("Should call only insert query before flush")
    void shouldCallOnlyInsertQuery() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent("testdata.simplerespository");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var person = new Person();
                person.setId(2L);
                person.setFirstName("Rake");
                person.setLastName("Tell");

                //when
                bibernateSession.save(Person.class, person);

                person.setFirstName("New Rake");
                bibernateSession.update(Person.class, person);
                bibernateSession.flush();

                //then
                assertQueries(bibernateSessionFactory, List.of(
                        "INSERT INTO persons ( id, first_name, last_name ) VALUES ( ?, ?, ? );"));

                var savedPerson = bibernateSession.findById(Person.class, 2L).orElseThrow();
                assertThat(savedPerson.getFirstName()).isEqualTo(person.getFirstName());
                assertThat(savedPerson.getLastName()).isEqualTo(person.getLastName());
                assertQueries(bibernateSessionFactory, List.of(
                        "INSERT INTO persons ( id, first_name, last_name ) VALUES ( ?, ?, ? );",
                        "SELECT * FROM persons WHERE id = ?;"));
            }
        }
    }

    @Test
    @DisplayName("Should call three insert query and delete one before flush")
    void ShouldCallThreeInsertQueryAndDeleteOne() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent("testdata.generatedvalue.identity");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var personJohn = preparePerson("John", "Smith");
                var personKate = preparePerson("Kate", "Lat");

                var persons = List.of(
                        personJohn,
                        preparePerson("Jane", "Doe"),
                        personKate,
                        preparePerson("Jan", "Ten"));

                //when
                bibernateSession.saveAll(testdata.generatedvalue.identity.Person.class, persons);

                personJohn.setFirstName("New John");
                bibernateSession.update(testdata.generatedvalue.identity.Person.class, personJohn);

                personKate.setId(10L);
                bibernateSession.deleteAll(testdata.generatedvalue.identity.Person.class, Collections.singleton(personKate));
                bibernateSession.flush();

                //then
                assertQueries(bibernateSessionFactory, List.of(
                        "INSERT INTO persons ( first_name, last_name ) VALUES ( ?, ? );",
                        "INSERT INTO persons ( first_name, last_name ) VALUES ( ?, ? );",
                        "INSERT INTO persons ( first_name, last_name ) VALUES ( ?, ? );",
                        "DELETE FROM persons WHERE id = ?;"));

                var savedPersonJohn = bibernateSession.findById(
                        testdata.generatedvalue.identity.Person.class, personJohn.getId()).orElseThrow();
                assertThat(persons).hasSize(4);
                assertThat(persons.get(0).getId()).isNotNull();
                assertThat(persons.get(0).getId()).isEqualTo(2L);
                assertThat(persons.get(0).getFirstName()).isEqualTo(savedPersonJohn.getFirstName());
                assertThat(persons.get(1).getId()).isNotNull();
                assertThat(persons.get(1).getId()).isEqualTo(3L);
                assertThat(persons.get(2).getId()).isNotNull();
                assertThat(persons.get(2).getId()).isEqualTo(10L);
                assertThat(persons.get(3).getId()).isNotNull();
                assertThat(persons.get(3).getId()).isEqualTo(4L);
                assertQueries(bibernateSessionFactory, List.of(
                        "INSERT INTO persons ( first_name, last_name ) VALUES ( ?, ? );",
                        "INSERT INTO persons ( first_name, last_name ) VALUES ( ?, ? );",
                        "INSERT INTO persons ( first_name, last_name ) VALUES ( ?, ? );",
                        "DELETE FROM persons WHERE id = ?;",
                        "SELECT * FROM persons WHERE id = ?;"));
            }
        }
    }

    private testdata.generatedvalue.identity.Person preparePerson(String firstName, String lastName) {
        var person = new testdata.generatedvalue.identity.Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);

        return person;
    }
}
