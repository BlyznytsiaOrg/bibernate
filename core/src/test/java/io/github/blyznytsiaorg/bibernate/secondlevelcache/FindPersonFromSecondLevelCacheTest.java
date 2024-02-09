package io.github.blyznytsiaorg.bibernate.secondlevelcache;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.findbyid.Person;
import testdata.update.immutable.PersonImmutable;

import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

class FindPersonFromSecondLevelCacheTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should put in cache only immutable entity")
    @Test
    void shouldPutInCacheOnlyImmutableEntity() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistentWithSecondLevelCache("testdata.findbyid");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                Optional<Person> person = bibernateSession.findById(Person.class, 1L);

                //then
                assertThat(person).isPresent();
                assertThat(person.get().getFirstName()).isEqualTo("FirstName");
                assertThat(person.get().getLastName()).isEqualTo("LastName");

                //then
                assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));
            }

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                Optional<Person> person = bibernateSession.findById(Person.class, 1L);

                //then
                assertThat(person).isPresent();
                assertThat(person.get().getFirstName()).isEqualTo("FirstName");
                assertThat(person.get().getLastName()).isEqualTo("LastName");

                //then
                assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));
            }
        }
    }

    @DisplayName("Should only cache immutable entity")
    @Test
    void shouldOnlyCacheImmutableEntity() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistentWithSecondLevelCache("testdata.update.immutable");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                Optional<PersonImmutable> person = bibernateSession.findById(PersonImmutable.class, 1L);

                //then
                assertThat(person).isPresent();
                assertThat(person.get().getFirstName()).isEqualTo("FirstName");
                assertThat(person.get().getLastName()).isEqualTo("LastName");

                //then
                assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));
            }

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                Optional<PersonImmutable> person = bibernateSession.findById(PersonImmutable.class, 1L);

                //then
                assertThat(person).isPresent();
                assertThat(person.get().getFirstName()).isEqualTo("FirstName");
                assertThat(person.get().getLastName()).isEqualTo("LastName");

                //then
                assertThat(bibernateSessionFactory.getExecutedQueries()).isEmpty();
            }
        }
    }

}
