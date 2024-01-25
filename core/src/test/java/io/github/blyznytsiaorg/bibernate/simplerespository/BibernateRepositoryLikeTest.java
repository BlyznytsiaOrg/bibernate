package io.github.blyznytsiaorg.bibernate.simplerespository;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.dao.SimpleRepositoryInvocationHandler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.simplerespository.Person;
import testdata.simplerespository.PersonRepository;

import java.util.Arrays;
import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.setupTables;

class BibernateRepositoryLikeTest extends AbstractPostgresInfrastructurePrep {


    @DisplayName("Should findByFirstNameLike using bibernate repository")
    @Test
    void shouldFindByFirstNameLike() {
        //given
        createTableWithData(4);

        List<Person> expectedPersons = Arrays.asList(
                createPerson("John4", "Doe4"),
                createPerson("John4", "Smith4")
        );

        var persistent = createPersistent();
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);
            //when
            List<Person> persons = personRepository.findByFirstNameLike("John%");

            //then
            Assertions.assertThat(persons).hasSize(expectedPersons.size())
                    .usingElementComparatorIgnoringFields("id")
                    .containsExactlyInAnyOrderElementsOf(expectedPersons);

            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE first_name like ?;"));
        }
    }

    private void createTableWithData(int i) {
        setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_GENERAL_INSERT_STATEMENT.formatted("John" + i, "Doe" + i));
        setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_GENERAL_INSERT_STATEMENT.formatted("Jane" + i, "Smith" + i));
        setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_GENERAL_INSERT_STATEMENT.formatted("John" + i, "Smith" + i));
        setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_GENERAL_INSERT_STATEMENT.formatted("Michael" + i, "Jones" + i));
    }

    private Person createPerson(String firstName, String lastName) {
        var person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        return person;
    }
}
