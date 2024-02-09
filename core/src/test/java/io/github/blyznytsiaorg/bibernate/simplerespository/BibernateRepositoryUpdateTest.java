package io.github.blyznytsiaorg.bibernate.simplerespository;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.dao.SimpleRepositoryInvocationHandler;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.simplerespository.Person;
import testdata.simplerespository.PersonRepository;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

class BibernateRepositoryUpdateTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should update using bibernate repository")
    @Test
    void shouldFindAll() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent();

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            var simpleRepositoryProxy = bibernateSessionFactory.getSimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);

            var personId = 1L;

            var updetePerson = new Person();
            updetePerson.setId(personId);
            updetePerson.setFirstName("Updated FirstName");
            updetePerson.setLastName("Updated LastName");

            //when
            var oldPerson = personRepository.findById(personId).orElseThrow();
            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));

            personRepository.update(updetePerson);
            assertQueries(bibernateSessionFactory, List.of(
                    "UPDATE persons SET first_name = ?, last_name = ? WHERE id = ?;"));

            var updatedPerson = personRepository.findById(personId).orElseThrow();

            //then
            assertThat(oldPerson.getId()).isEqualTo(updatedPerson.getId());
            assertThat(oldPerson.getFirstName()).isNotEqualTo(updatedPerson.getFirstName());
            assertThat(oldPerson.getLastName()).isNotEqualTo(updatedPerson.getLastName());
            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));
        }
    }
}
