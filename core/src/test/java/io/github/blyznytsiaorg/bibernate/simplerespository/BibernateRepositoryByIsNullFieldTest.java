package io.github.blyznytsiaorg.bibernate.simplerespository;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.dao.SimpleRepositoryInvocationHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.simplerespository.User;
import testdata.simplerespository.UserRepository;

import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.setupTables;
import static org.assertj.core.api.Assertions.assertThat;

class BibernateRepositoryByIsNullFieldTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should findByUsernameNull using bibernate repository")
    @Test
    void findByUsernameNull() {
        //given
        createTableWithData(5);

        List<User> expectedPersons = List.of(
                createUser(null, true, 12)
        );

        var persistent = createPersistent("testdata.simplerespository");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = bibernateSessionFactory.getSimpleRepositoryInvocationHandler();
            var userRepository = simpleRepositoryProxy.registerRepository(UserRepository.class);
            //when
            List<User> users = userRepository.findByUsernameNull();

            //then
            assertThat(users).hasSize(expectedPersons.size())
                    .usingElementComparatorIgnoringFields("id")
                    .containsExactlyInAnyOrderElementsOf(expectedPersons);

            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM users WHERE username is null;"));
        }
    }

    @DisplayName("Should findByUsernameAndAge using bibernate repository")
    @Test
    void findByUsernameAndAgeWithOptionalReturn() {
        //given
        createTableWithData(5);

        List<User> expectedPersons = List.of(
                createUser(null, true, 12)
        );

        var persistent = createPersistent("testdata.simplerespository");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var simpleRepositoryProxy = bibernateSessionFactory.getSimpleRepositoryInvocationHandler();
            var userRepository = simpleRepositoryProxy.registerRepository(UserRepository.class);
            //when
            Optional<User> user = userRepository.findByUsernameAndAge("Levik5", 18);

            //then
            assertThat(user).isPresent();

            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM users WHERE username = ? And age = ?;"));
        }
    }

    private void createTableWithData(int i) {
        setupTables(dataSource, CREATE_USERS_TABLE, CREATE_USERS_GENERAL_INSERT_STATEMENT.formatted("Levik" + i,true, 18));
        setupTables(dataSource, CREATE_USERS_TABLE, CREATE_USERS_GENERAL_INSERT_STATEMENT.formatted("Nic" + i, false,  16));
        setupTables(dataSource, CREATE_USERS_TABLE, CREATE_USERS_GENERAL_INSERT_STATEMENT.formatted("John" + i,true, 21));
        setupTables(dataSource, CREATE_USERS_TABLE, CREATE_USERS_WITH_NULL_USERNAME_INSERT_STATEMENT.formatted( true, 12));
    }

    private User createUser(String username, boolean enabled, int age) {
        var user = new User();
        user.setUsername(username);
        user.setAge(age);
        user.setEnabled(enabled);
        return user;
    }

}
