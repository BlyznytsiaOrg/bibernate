package io.github.blyznytsiaorg.bibernate.update;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.exception.ImmutableEntityException;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import testdata.findbyid.Person;
import testdata.update.immutable.PersonImmutable;

import java.util.List;
import java.util.UUID;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImmutableEntityTest extends AbstractPostgresInfrastructurePrep {
    @DisplayName("Should not update entity that marked as Immutable")
    @Test
    void shouldNotUpdateEntityThatMarkedAsImmutable() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);

        var persistent = createPersistent("testdata.update.immutable");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            String uuid = UUID.randomUUID().toString();
            String firstName;
            String lastName;

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                var person = bibernateSession.findById(PersonImmutable.class, 1L).orElseThrow();
                firstName = person.getFirstName();
                lastName = person.getLastName();
                person.setFirstName(person.getFirstName() + uuid);
            }

            //then
            assertQueries(bibernateSessionFactory, List.of("SELECT persons.id as persons_id, persons.first_name as persons_first_name, persons.last_name as persons_last_name FROM persons WHERE persons.id = ?;"));

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                var person = bibernateSession.findById(PersonImmutable.class, 1L).orElseThrow();

                //then
                assertThat(person.getFirstName()).isEqualTo(firstName);
                assertThat(person.getLastName()).isEqualTo(lastName);
            }

            //then
            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));
        }
    }

    @DisplayName("Should not save entity that marked as Immutable")
    @Test
    void shouldNotSaveEntityThatMarkedAsImmutable() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);

        var persistent = createPersistent("testdata.update.immutable");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            Executable executable = () -> {
                try (var bibernateSession = bibernateSessionFactory.openSession()) {
                    //when
                    var personImmutable = new PersonImmutable();
                    personImmutable.setId(2L);
                    personImmutable.setLastName("LastName2");
                    personImmutable.setFirstName("FirstName2");

                    bibernateSession.save(PersonImmutable.class, personImmutable);
                }
            };

            //then
            assertThat(bibernateSessionFactory.getExecutedQueries()).isEmpty();

            var entityStateWasChangeException = assertThrows(ImmutableEntityException.class, executable);
            assertThat(entityStateWasChangeException.getMessage())
                    .isEqualTo("Immutable entity class testdata.update.immutable.PersonImmutable not allowed to change");
        }
    }

    @DisplayName("Should not delete entity that marked as Immutable")
    @Test
    void shouldNotDeleteEntityThatMarkedAsImmutable() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);

        var persistent = createPersistent("testdata.update.immutable");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                bibernateSession.deleteById(PersonImmutable.class, 1L);
            }

            //then
            assertThat(bibernateSessionFactory.getExecutedQueries()).isEmpty();

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                var person = bibernateSession.findById(PersonImmutable.class, 1L).orElseThrow();
            }

            //then
            assertQueries(bibernateSessionFactory, List.of("SELECT persons.id as persons_id, persons.first_name as persons_first_name, persons.last_name as persons_last_name FROM persons WHERE persons.id = ?;"));
        }
    }

}
