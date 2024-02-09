package io.github.blyznytsiaorg.bibernate.generatedvalue;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.generatedvalue.none.Person;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

class NoneTest extends AbstractPostgresInfrastructurePrep {

    @Test
    @DisplayName("Should get id from the entity")
    void shouldGenerateIdFromEntity() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent();

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var person = new Person();
                person.setId(17l);
                person.setFirstName("John");
                person.setLastName("Smith");

                //when
                var savedPerson = bibernateSession.save(Person.class, person);

                //then
                assertThat(savedPerson).isNotNull();
                assertThat(savedPerson.getId()).isNotNull();
                assertThat(savedPerson.getId()).isEqualTo(17L);
            }

            //then
            assertQueries(bibernateSessionFactory, List.of(
                    "INSERT INTO persons ( id, first_name, last_name ) VALUES ( ?, ?, ? );"));
        }
    }

    @Test
    @DisplayName("Should get id from the entities")
    void shouldGenerateIdFromEntities() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent();

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var persons = List.of(
                        preparePerson(34L, "John", "Smith"),
                        preparePerson(55L, "Jane", "Doe"),
                        preparePerson(33L, "Kate", "Lat"),
                        preparePerson(41L, "Jan", "Ten"));

                //when
                bibernateSession.saveAll(Person.class, persons);
                bibernateSession.flush();

                //then
                assertThat(persons).hasSize(4);
                assertThat(persons.get(0).getId()).isNotNull();
                assertThat(persons.get(0).getId()).isEqualTo(34L);
                assertThat(persons.get(1).getId()).isNotNull();
                assertThat(persons.get(1).getId()).isEqualTo(55L);
                assertThat(persons.get(2).getId()).isNotNull();
                assertThat(persons.get(2).getId()).isEqualTo(33L);
                assertThat(persons.get(3).getId()).isNotNull();
                assertThat(persons.get(3).getId()).isEqualTo(41L);
                assertQueries(bibernateSessionFactory, List.of(
                        "INSERT INTO persons ( id, first_name, last_name ) VALUES ( ?, ?, ? );",
                        "INSERT INTO persons ( id, first_name, last_name ) VALUES ( ?, ?, ? );",
                        "INSERT INTO persons ( id, first_name, last_name ) VALUES ( ?, ?, ? );",
                        "INSERT INTO persons ( id, first_name, last_name ) VALUES ( ?, ?, ? );"));
            }
        }
    }

    private Person preparePerson(Long id, String firstName, String lastName) {
        var person = new Person();
        person.setId(id);
        person.setFirstName(firstName);
        person.setLastName(lastName);

        return person;
    }
}
