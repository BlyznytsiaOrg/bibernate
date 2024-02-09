package io.github.blyznytsiaorg.bibernate.transaction;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.generatedvalue.identity.Person;

public class BibernateSessionTransactionTest extends AbstractPostgresInfrastructurePrep {

    @Test
    @DisplayName("Should not save changes after rollback")
    void shouldNotSaveChangesAfterRollback() throws SQLException {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent();

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                bibernateSession.startTransaction();
                var person1 = new Person();
                person1.setFirstName("John");
                person1.setLastName("Smith");

                var person2 = new Person();
                person2.setFirstName("Jane");
                person2.setLastName("Smith");

                //when
                var savedPerson1 = bibernateSession.save(Person.class, person1);
                var savedPerson2 = bibernateSession.save(Person.class, person2);
                bibernateSession.flush();

                bibernateSession.rollbackTransaction();
                var PersonFromDb = bibernateSession.findById(Person.class, 2L);

                //then
                assertThat(PersonFromDb.isEmpty()).isTrue();

                //then
                assertThat(savedPerson1).isNotNull();
                assertThat(savedPerson1.getId()).isNull();

                assertThat(savedPerson2).isNotNull();
                assertThat(savedPerson2.getId()).isNull();

                assertQueries(bibernateSessionFactory, List.of(
                        "INSERT INTO persons ( first_name, last_name ) VALUES ( ?, ? );",
                        "INSERT INTO persons ( first_name, last_name ) VALUES ( ?, ? );",
                        "SELECT * FROM persons WHERE id = ?;"));
            }
        }
    }

    @Test
    @DisplayName("Should save changes only after commit")
    void shouldSaveChangesOnlyAfterCommit() throws SQLException {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent();

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                bibernateSession.startTransaction();
                var person1 = new Person();
                person1.setFirstName("John");
                person1.setLastName("Smith");

                var person2 = new Person();
                person2.setFirstName("Jane");
                person2.setLastName("Smith");

                //when
                var savedPerson1 = bibernateSession.save(Person.class, person1);
                var savedPerson2 = bibernateSession.save(Person.class, person2);
                bibernateSession.flush();

                bibernateSession.commitTransaction();

                var personFromDbAfterCommit = bibernateSession.findById(Person.class, 2L).get();

                //then

                assertThat(personFromDbAfterCommit).isNotNull();
                assertThat(personFromDbAfterCommit.getId()).isNotNull();
                assertThat(savedPerson1.getId()).isEqualTo(2L);

                assertThat(savedPerson1).isNotNull();
                assertThat(savedPerson1.getId()).isNotNull();
                assertThat(savedPerson1.getId()).isEqualTo(2L);

                assertThat(savedPerson2).isNotNull();
                assertThat(savedPerson2.getId()).isNotNull();
                assertThat(savedPerson2.getId()).isEqualTo(3L);

                assertQueries(bibernateSessionFactory, List.of(
                        "INSERT INTO persons ( first_name, last_name ) VALUES ( ?, ? );",
                        "INSERT INTO persons ( first_name, last_name ) VALUES ( ?, ? );",
                        "SELECT * FROM persons WHERE id = ?;"));
            }
        }
    }
}
