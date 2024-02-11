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

class BibernateRepositoryByNotNullFieldTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should findByUsernameNotNull using bibernate repository")
    @Test
    void findByUsernameNotNull() {
        //given
        createTableWithData(5);

        List<User> expectedPersons = Arrays.asList(
                createUser("Levik5", true, 18),
                createUser("Nic5", false, 16),
                createUser("John5", true, 21),
                createUser("Michael5", true, 12)
        );

        var persistent = createPersistent("testdata.simplerespository");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = bibernateSessionFactory.getSimpleRepositoryInvocationHandler();
            var userRepository = simpleRepositoryProxy.registerRepository(UserRepository.class);
            //when
            List<User> users = userRepository.findByUsernameNotNull();

            //then
            Assertions.assertThat(users).hasSize(expectedPersons.size())
                    .usingElementComparatorIgnoringFields("id")
                    .containsExactlyInAnyOrderElementsOf(expectedPersons);

            assertQueries(bibernateSessionFactory, List.of("SELECT users.id AS users_id, users.username AS users_username, users.enabled AS users_enabled, users.age AS users_age FROM users WHERE username is not null;"));
        }
    }

    private void createTableWithData(int i) {
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
