package io.github.blyznytsiaorg.bibernate.simplerespository;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.dao.SimpleRepositoryInvocationHandler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.simplerespository.User;
import testdata.simplerespository.UserRepository;

import java.util.Arrays;
import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.setupTables;

class BibernateRepositoryByBooleanTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should findByEnabledTrue using bibernate repository")
    @Test
    void shouldFindByEnabledTrue() {
        //given
        createTableWithData(4);

        List<User> expectedUsers = Arrays.asList(
                createUser("Levik4", true, 18),
                createUser("John4", true, 21),
                createUser("Michael4", true, 12)
        );


        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = new SimpleRepositoryInvocationHandler();
            var userRepository = simpleRepositoryProxy.registerRepository(UserRepository.class);
            //when
            List<User> users = userRepository.findByEnabled(true);

            //then
            Assertions.assertThat(users).hasSize(expectedUsers.size())
                    .usingElementComparatorIgnoringFields("id")
                    .containsExactlyInAnyOrderElementsOf(expectedUsers);

            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM users WHERE enabled = ?;"));
        }
    }

    private static void createTableWithData(int i) {
        setupTables(dataSource, CREATE_USERS_TABLE, CREATE_USERS_GENERAL_INSERT_STATEMENT.formatted("Levik" + i,true, 18));
        setupTables(dataSource, CREATE_USERS_TABLE, CREATE_USERS_GENERAL_INSERT_STATEMENT.formatted("Nic" + i, false,  16));
        setupTables(dataSource, CREATE_USERS_TABLE, CREATE_USERS_GENERAL_INSERT_STATEMENT.formatted("John" + i,true, 21));
        setupTables(dataSource, CREATE_USERS_TABLE, CREATE_USERS_GENERAL_INSERT_STATEMENT.formatted("Michael" + i, true, 12));
    }

    private User createUser(String username, boolean enabled, int age) {
        var user = new User();
        user.setUsername(username);
        user.setAge(age);
        user.setEnabled(enabled);
        return user;
    }

}
