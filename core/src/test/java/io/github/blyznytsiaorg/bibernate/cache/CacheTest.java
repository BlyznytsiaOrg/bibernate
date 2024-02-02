package io.github.blyznytsiaorg.bibernate.cache;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.simplerespository.Person;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

class CacheTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should Update entity in DB, but return cached entity")
    @Test
    void shouldUpdateEntityInDBButReturnCachedEntity() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent();

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var personId = 1L;

                //when
                bibernateSession.findByQuery(
                        Person.class, "SELECT * FROM persons WHERE id = ?;", new Object[]{personId});

                var updetePerson = new Person();
                updetePerson.setId(personId);
                updetePerson.setFirstName("Updated FirstName");
                updetePerson.setLastName("Updated LastName");

                var resultOfUpdate = bibernateSession.getDao().update(Person.class, updetePerson, List.of());

                var personFromCache = bibernateSession.findByQuery(
                        Person.class, "SELECT * FROM persons WHERE id = ?;", new Object[]{personId});

                //then
                assertThat(resultOfUpdate).isNotZero();
                assertThat(personFromCache).isNotEmpty().allSatisfy(person -> {
                   assertThat(person.getId()).isEqualTo(updetePerson.getId());
                   assertThat(person.getFirstName()).isNotEqualTo(updetePerson.getFirstName());
                   assertThat(person.getLastName()).isNotEqualTo(updetePerson.getLastName());
                });
                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM persons WHERE id = ?;",
                        "UPDATE persons SET first_name = ?, last_name = ? WHERE id = ?;",
                        "SELECT * FROM persons WHERE id = ?;"));
            }
        }
    }

    @DisplayName("Should Update entity in DB, and execute flush")
    @Test
    void shouldUpdateEntityInDBAndExecuteFlush() {
        // given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent();

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var personId = 1L;

                //when
                bibernateSession.findByQuery(
                        Person.class, "SELECT * FROM persons WHERE id = ?;", new Object[]{personId});

                var updetePerson = new Person();
                updetePerson.setId(personId);
                updetePerson.setFirstName("Updated FirstName");
                updetePerson.setLastName("Updated LastName");

                var resultOfUpdate = bibernateSession.update(Person.class, updetePerson);

                bibernateSession.flush();

                var personFromCache = bibernateSession.findByQuery(
                        Person.class, "SELECT * FROM persons WHERE id = ?;", new Object[]{personId});

                //then
                assertThat(resultOfUpdate).isNotZero();
                assertThat(personFromCache).isNotEmpty().allSatisfy(person -> {
                    assertThat(person.getId()).isEqualTo(updetePerson.getId());
                    assertThat(person.getFirstName()).isEqualTo(updetePerson.getFirstName());
                    assertThat(person.getLastName()).isEqualTo(updetePerson.getLastName());
                });
                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM persons WHERE id = ?;",
                        "UPDATE persons SET first_name = ?, last_name = ? WHERE id = ?;",
                        "SELECT * FROM persons WHERE id = ?;"));
            }
        }
    }
}
