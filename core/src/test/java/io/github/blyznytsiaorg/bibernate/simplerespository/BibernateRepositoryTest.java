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
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.setupTables;

class BibernateRepositoryTest extends AbstractPostgresInfrastructurePrep  {

    @DisplayName("Should findById")
    @Test
    void shouldFindById() {
        //given
        createTableWithData(4);

        var persistent = createPersistent();
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);
            //when
            Optional<Person> persons = personRepository.findById(1L);

            //then
            Assertions.assertThat(persons).isPresent();
            Assertions.assertThat(persons).get().isNotNull();

            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));
        }
    }

    @DisplayName("Should findByFirstNameEquals using bibernate repository and return empty if nothing found")
    @Test
    void shouldFindByFirstNameEqualsAndReturnEmptyIfNothingFound() {
        //given
        createTableWithData(3);

        var persistent = createPersistent();
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);
            //when
            List<Person> persons = personRepository.findByFirstNameEquals("John");

            //then
            Assertions.assertThat(persons).hasSize(0);

            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE first_name = ?;"));
        }
    }

    @DisplayName("Should findByFirstNameEquals using bibernate repository")
    @Test
    void shouldFindByFirstNameEquals() {
        //given
        createTableWithData(2);

        List<Person> expectedPersons = Arrays.asList(
                createPerson("John2", "Doe2"),
                createPerson("John2", "Smith2")
        );


        var persistent = createPersistent();
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);
            //when
            List<Person> persons = personRepository.findByFirstNameEquals("John2");

            //then
            Assertions.assertThat(persons).hasSize(expectedPersons.size())
                    .usingElementComparatorIgnoringFields("id")
                    .containsExactlyInAnyOrderElementsOf(expectedPersons);

            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE first_name = ?;"));
        }
    }

    @DisplayName("Should findByFirstNameOrLastName using bibernate repository")
    @Test
    void shouldFindByFirstNameOrLastName() {
        //given
        createTableWithData(1);

        List<Person> expectedPersons = Arrays.asList(
                createPerson("John1", "Doe1"),
                createPerson("John1", "Smith1"),
                createPerson("Michael1", "Jones1")
        );


        var persistent = createPersistent();
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);
            //when
            List<Person> persons = personRepository.findByFirstNameOrLastName("John1", "Jones1");

            //then
            Assertions.assertThat(persons).hasSize(3)
                    .usingElementComparatorIgnoringFields("id")
                    .containsExactlyInAnyOrderElementsOf(expectedPersons);

            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE first_name = ? Or last_name = ?;"));
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
