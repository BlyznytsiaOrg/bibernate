package io.github.blyznytsiaorg.bibernate.simplerespository;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.dao.SimpleRepositoryInvocationHandler;
import io.github.blyznytsiaorg.bibernate.exception.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import testdata.simplerespository.Person;
import testdata.simplerespository.PersonRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.setupTables;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BibernateRepositoryTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should findById")
    @Test
    void shouldFindById() {
        //given
        createTableWithData(4);

        var persistent = createPersistent("testdata");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);
            //when
            Optional<Person> persons = personRepository.findById(1L);

            //then
            assertThat(persons).isPresent();
            assertThat(persons).get().isNotNull();

            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));
        }
    }

    @DisplayName("Should findByFirstNameEquals using bibernate repository and return empty if nothing found")
    @Test
    void shouldFindByFirstNameEqualsAndReturnEmptyIfNothingFound() {
        //given
        createTableWithData(3);

        var persistent = createPersistent("testdata");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);
            //when
            List<Person> persons = personRepository.findByFirstNameEquals("John");

            //then
            assertThat(persons).isEmpty();

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


        var persistent = createPersistent("testdata");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);
            //when
            List<Person> persons = personRepository.findByFirstNameEquals("John2");

            //then
            assertThat(persons).hasSize(expectedPersons.size())
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


        var persistent = createPersistent("testdata");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);
            //when
            List<Person> persons = personRepository.findByFirstNameOrLastName("John1", "Jones1");

            //then
            assertThat(persons).hasSize(3)
                    .usingElementComparatorIgnoringFields("id")
                    .containsExactlyInAnyOrderElementsOf(expectedPersons);

            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE first_name = ? Or last_name = ?;"));
        }
    }

    @Test
    void shouldFindByFirstNameViaHQL() {
        //given
        createTableWithData(1);

        List<Person> expectedPersons = Arrays.asList(
                createPerson("John1", "Doe1"),
                createPerson("John1", "Smith1")
        );


        var persistent = createPersistent("testdata");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);
            //when
            List<Person> persons = personRepository.findByFirstName("John1");

            //then
            assertThat(persons).hasSize(expectedPersons.size())
                    .usingElementComparatorIgnoringFields("id")
                    .containsExactlyInAnyOrderElementsOf(expectedPersons);

            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE first_name = ?;"));
        }
    }

    @DisplayName("Should saveAll via repositories")
    @Test
    void shouldSaveAll() {
        //given
        createTableWithData(4);

        var persistent = createPersistent("testdata");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);

            //when
            var newPerson1 = createPerson(7L, "NEW_FIRST_NAME7", "NEW_LAST_NAME7");
            var newPerson2 = createPerson(8L, "NEW_FIRST_NAME8", "NEW_LAST_NAME8");

            List<Person> persons = new ArrayList<>();
            persons.add(newPerson1);
            persons.add(newPerson2);

            personRepository.saveAll(persons);

            //then
            assertThat(personRepository.findAll()).hasSize(6);
            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons;"));
        }
    }

    @DisplayName("Should findOne method without optional")
    @Test
    void shouldFindOneMethodWithoutOptional() {
        //given
        createTableWithData(4);

        var persistent = createPersistent("testdata");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);
            //when
            Person person = personRepository.findOne(1L);

            assertThat(person).isNotNull();

            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));
        }
    }

    @DisplayName("Should not findOne entity and get exception")
    @Test
    void shouldNotFindOneEntityAndGetException() {
        //given
        createTableWithData(4);

        var persistent = createPersistent("testdata");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);
            //when
            Executable executable = () -> personRepository.findOne(5L);


            //then
            var entityStateWasChangeException = assertThrows(EntityNotFoundException.class, executable);
            assertThat(entityStateWasChangeException.getMessage())
                    .isEqualTo("Entity Person not found by ID 5");
            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));
        }
    }

    @DisplayName("Should save new entity using repository save")
    @Test
    void shouldSaveNewEntityUsingRepositorySave() {
        //given
        createTableWithData(4);

        var persistent = createPersistent("testdata");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);

            //when
            var newPerson = createPerson(7L, "NEW_FIRST_NAME", "NEW_LAST_NAME");

            Person savePerson = personRepository.save(newPerson);

            //then
            assertThat(savePerson).isNotNull();
            assertThat(savePerson.getId()).isNotNull();
            assertThat(savePerson.getLastName()).isEqualTo("NEW_LAST_NAME");
            assertThat(savePerson.getFirstName()).isEqualTo("NEW_FIRST_NAME");

            assertQueries(bibernateSessionFactory, List.of("INSERT INTO persons ( id, first_name, last_name ) VALUES ( ?, ?, ? );"));
        }
    }

    @DisplayName("Should delete via repository by Id")
    @Test
    void shouldDeleteViaRepositoryById() {
        //given
        createTableWithData(4);

        var persistent = createPersistent("testdata");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);

            //when
            personRepository.delete(1L);
            assertQueries(bibernateSessionFactory, List.of(
                    "SELECT * FROM persons WHERE id = ?;", 
                    "DELETE FROM persons WHERE id = ?;"));

            //then
            assertThat(personRepository.findAll()).hasSize(3);
            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons;"));
        }
    }

    @DisplayName("Should deleteAll via repository by Ids")
    @Test
    void shouldDeleteAllViaRepositoryByIds() {
        //given
        createTableWithData(4);

        var persistent = createPersistent("testdata");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);

            //when
            List<Long> ids = Arrays.asList(1L, 2L);
            personRepository.deleteAll(ids);
            assertQueries(bibernateSessionFactory, List.of(
                    "SELECT * FROM persons WHERE id = ?;",
                    "DELETE FROM persons WHERE id = ?;",
                    "SELECT * FROM persons WHERE id = ?;",
                    "DELETE FROM persons WHERE id = ?;"
            ));

            //then
            assertThat(personRepository.findAll()).hasSize(2);
            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons;"));
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

    private Person createPerson(Long id, String firstName, String lastName) {
        var newPerson = new Person();
        newPerson.setId(id);
        newPerson.setFirstName(firstName);
        newPerson.setLastName(lastName);
        return newPerson;
    }
}
