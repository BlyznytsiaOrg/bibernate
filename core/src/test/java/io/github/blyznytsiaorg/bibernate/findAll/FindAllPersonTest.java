package io.github.blyznytsiaorg.bibernate.findAll;


import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.simplerespository.Person;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.setupTables;
import static org.assertj.core.api.Assertions.assertThat;

class FindAllPersonTest extends AbstractPostgresInfrastructurePrep {

    @Test
    @DisplayName("Should find All person")
    void shouldFindAllPerson() {
        //given
        createTableWithData(3);
        var persistent = createPersistent("testdata.simplerespository");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var persons = bibernateSession.findAll(Person.class);

                assertThat(persons).isNotEmpty();
                assertThat(persons).hasSize(3);
                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM persons;"));
            }
        }
    }

    @Test
    @DisplayName("Should find All By Id person")
    void shouldFindAllByIdPerson() {
        //given
        createTableWithData(3);
        var persistent = createPersistent("testdata.simplerespository");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var persons = bibernateSession.findAllById(Person.class, Set.of(1, 2, 3));

                assertThat(persons).isNotEmpty();
                assertThat(persons).hasSize(3);
                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM persons WHERE id IN ( ?, ?, ? );"));
            }
        }
    }

    private void createTableWithData(int i) {
        setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_GENERAL_INSERT_STATEMENT.formatted("John" + i, "Doe" + i));
        setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_GENERAL_INSERT_STATEMENT.formatted("Jane" + i, "Smith" + i));
        setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_GENERAL_INSERT_STATEMENT.formatted("John" + i, "Smith" + i));
    }
}
