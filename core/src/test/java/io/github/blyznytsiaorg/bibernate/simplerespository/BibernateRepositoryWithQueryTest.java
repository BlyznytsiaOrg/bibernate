package io.github.blyznytsiaorg.bibernate.simplerespository;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.dao.SimpleRepositoryInvocationHandler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import testdata.simplerespository.UserRepository;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.setupTables;

class BibernateRepositoryWithQueryTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should countUserDuplicate using bibernate repository with query")
    @Test
    void countUserDuplicate() {
        //given
        createTableWithData(5);

        var persistent = createPersistent("");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var userRepository = simpleRepositoryProxy.registerRepository(UserRepository.class);
            //when
            long userDuplicateCount = userRepository.countUserDuplicate(1);

            //then
            Assertions.assertThat(userDuplicateCount).isEqualTo(2);

            assertQueries(bibernateSessionFactory, List.of("select count(*) from users group by username having count(username) > ?"));
        }
    }

    private void createTableWithData(int i) {
        setupTables(dataSource, CREATE_USERS_TABLE, CREATE_USERS_GENERAL_INSERT_STATEMENT.formatted("Levik" + i,true, 18));
        setupTables(dataSource, CREATE_USERS_TABLE, CREATE_USERS_GENERAL_INSERT_STATEMENT.formatted("Nic" + i, false,  16));
        setupTables(dataSource, CREATE_USERS_TABLE, CREATE_USERS_GENERAL_INSERT_STATEMENT.formatted("Levik" + i,true, 21));
        setupTables(dataSource, CREATE_USERS_TABLE, CREATE_USERS_GENERAL_INSERT_STATEMENT.formatted("Michael" + i, true, 12));
    }

}
