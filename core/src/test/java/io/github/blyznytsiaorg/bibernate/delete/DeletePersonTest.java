package io.github.blyznytsiaorg.bibernate.delete;


import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.simplerespository.Person;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

class DeletePersonTest extends AbstractPostgresInfrastructurePrep {

    @Test
    @DisplayName("Should delete person by ID")
    void shouldDeletePersonById() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent("");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                //when
                bibernateSession.deleteById(Person.class, 1L);

                //then
                var person = bibernateSession.findById(Person.class, 1L);

                assertThat(person.isEmpty()).isTrue();
                assertQueries(bibernateSessionFactory, List.of(
                        "DELETE FROM persons WHERE id = ?;",
                        "SELECT * FROM persons WHERE id = ?;"));
            }
        }
    }

    @Test
    @DisplayName("Should delete person by entity")
    void shouldDeletePersonByEntity() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent("");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var person = bibernateSession.findById(Person.class, 1L).orElseThrow();

                //when
                bibernateSession.delete(Person.class, person);

                //then
                var deletedPerson = bibernateSession.findById(Person.class, 1L);

                assertThat(deletedPerson.isEmpty()).isTrue();
                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM persons WHERE id = ?;",
                        "DELETE FROM persons WHERE id = ?;",
                        "SELECT * FROM persons WHERE id = ?;"));
            }
        }
    }
}
