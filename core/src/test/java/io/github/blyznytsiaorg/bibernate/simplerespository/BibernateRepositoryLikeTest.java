package io.github.blyznytsiaorg.bibernate.simplerespository;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.dao.SimpleRepositoryInvocationHandler;
import io.github.blyznytsiaorg.bibernate.exception.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import testdata.simplerespository.Person;
import testdata.simplerespository.PersonRepository;

import java.util.Arrays;
import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.setupTables;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        var persistent = createPersistent("testdata.simplerespository");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = bibernateSessionFactory.getSimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);
            //when
            List<Person> persons = personRepository.findByFirstNameLike("John%");

            //then
            assertThat(persons).hasSize(expectedPersons.size())
                    .usingElementComparatorIgnoringFields("id")
                    .containsExactlyInAnyOrderElementsOf(expectedPersons);

            assertQueries(bibernateSessionFactory, List.of("SELECT persons.id AS persons_id, persons.first_name AS persons_first_name, persons.last_name AS persons_last_name FROM persons WHERE first_name like ?;"));
        }
    }


    @DisplayName("Should findByFirstNameAndLastName using bibernate repository")
    @Test
    void shouldFindByFirstNameAndLastNameOnlyOne() {
        //given
        createTableWithData(4);

        var persistent = createPersistent("testdata.simplerespository");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = bibernateSessionFactory.getSimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);
            //when
            Person person = personRepository.findByFirstNameAndLastName("John4", "Doe4");

            //then
            assertThat(person).isNotNull();
            assertQueries(bibernateSessionFactory, List.of("SELECT persons.id AS persons_id, persons.first_name AS persons_first_name, persons.last_name AS persons_last_name FROM persons WHERE first_name = ? And last_name = ?;"));
        }
    }

    @DisplayName("Should findByFirstNameAndLastNameThrowEntityNotFoundException using bibernate repository")
    @Test
    void shouldFindByFirstNameAndLastNameThrowEntityNotFoundException() {
        //given
        createTableWithData(4);

        var persistent = createPersistent("testdata.simplerespository");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);
            //when
            Executable executable = () -> personRepository.findByFirstNameAndLastName("John1", "Doe1");

            //then
            var entityNotFoundException = assertThrows(EntityNotFoundException.class, executable);
            assertThat(entityNotFoundException.getMessage())
                    .isEqualTo("Cannot find result for Person in method findByFirstNameAndLastName parameters [John1, Doe1]");
            assertQueries(bibernateSessionFactory, List.of("SELECT persons.id AS persons_id, persons.first_name AS persons_first_name, persons.last_name AS persons_last_name FROM persons WHERE first_name = ? And last_name = ?;"));
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
