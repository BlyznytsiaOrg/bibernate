package io.github.blyznytsiaorg.bibernate.simplerespository;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.dao.SimpleRepositoryInvocationHandler;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.simplerespository.Person;
import testdata.simplerespository.PersonRepository;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.setupTables;

@Slf4j
class BibernateCustomRepositoryTest extends AbstractPostgresInfrastructurePrep  {

    @DisplayName("Should call custom repository method")
    @Test
    void shouldCallCustomRepositoryMethod() {
        //given
        try {
            createTableWithData(5);

            try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
                var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

                var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler(bibernateSessionFactory);
                var personRepository = simpleRepositoryProxy.registerRepository(PersonRepository.class);
                //when
                List<Person> persons = personRepository.findMyCustomQuery();

                //then
                Assertions.assertThat(persons).hasSize(1);
                assertQueries(bibernateSessionFactory, List.of("SELECT * FROM persons WHERE id = ?;"));
            }
        } catch (Exception exe) {
            log.info("Some exception on CI ", exe);
        }
    }

    private static void createTableWithData(int i) {
        setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_GENERAL_INSERT_STATEMENT.formatted("John" + i, "Doe" + i));
        setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_GENERAL_INSERT_STATEMENT.formatted("Jane" + i, "Smith" + i));
        setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_GENERAL_INSERT_STATEMENT.formatted("John" + i, "Smith" + i));
        setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_GENERAL_INSERT_STATEMENT.formatted("Michael" + i, "Jones" + i));
    }
}
