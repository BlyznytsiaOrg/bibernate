package io.github.blyznytsiaorg.bibernate.generatedvalue;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.generatedvalue.sequence.Person;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

class SequenceTest extends AbstractPostgresInfrastructurePrep {

    @Test
    @DisplayName("Should generate id from proper sequence and set it to entity")
    void shouldGenerateIdFromSequenceAndSetItToEntity() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        QueryUtils.setupSequences(dataSource, CREATE_PERSON_ID_SEQUENCE);
        var persistent = createPersistent();

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var person = new Person();
                person.setFirstName("John");
                person.setLastName("Smith");

                //when
                var savedPerson = bibernateSession.save(Person.class, person);
                bibernateSession.flush();

                //then
                assertThat(savedPerson).isNotNull();
                assertThat(savedPerson.getId()).isNotNull();
                assertThat(savedPerson.getId()).isEqualTo(2L);
                assertQueries(bibernateSessionFactory, List.of(
                        //"select next value for persons_id_seq;",
                        "select nextval('persons_id_seq');",
                        "INSERT INTO persons ( id, first_name, last_name ) VALUES ( ?, ?, ? );"));
            }
        }
    }

    @Test
    @DisplayName("Should generate id from proper sequence and set it to entities")
    void shouldGenerateIdFromSequenceAndSetItToEntities() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        QueryUtils.setupSequences(dataSource, CREATE_PERSON_ID_SEQUENCE);
        var persistent = createPersistent();

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var persons = List.of(
                        preparePerson("John", "Smith"),
                        preparePerson("Jane", "Doe"),
                        preparePerson("Kate", "Lat"),
                        preparePerson("Jan", "Ten"));

                //when
                bibernateSession.saveAll(Person.class, persons);
                bibernateSession.flush();

                //then
                assertThat(persons).hasSize(4);
                assertThat(persons.get(0).getId()).isNotNull();
                assertThat(persons.get(0).getId()).isEqualTo(2L);
                assertThat(persons.get(1).getId()).isNotNull();
                assertThat(persons.get(1).getId()).isEqualTo(3L);
                assertThat(persons.get(2).getId()).isNotNull();
                assertThat(persons.get(2).getId()).isEqualTo(4L);
                assertThat(persons.get(3).getId()).isNotNull();
                assertThat(persons.get(3).getId()).isEqualTo(5L);
                assertQueries(bibernateSessionFactory, List.of(
                        "select nextval('persons_id_seq');",
                        "INSERT INTO persons ( id, first_name, last_name ) VALUES ( ?, ?, ? );",
                        "select nextval('persons_id_seq');",
                        "INSERT INTO persons ( id, first_name, last_name ) VALUES ( ?, ?, ? );",
                        "select nextval('persons_id_seq');",
                        "INSERT INTO persons ( id, first_name, last_name ) VALUES ( ?, ?, ? );",
                        "select nextval('persons_id_seq');",
                        "INSERT INTO persons ( id, first_name, last_name ) VALUES ( ?, ?, ? );"));
            }
        }
    }

    private Person preparePerson(String firstName, String lastName) {
        var person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);

        return person;
    }
}
