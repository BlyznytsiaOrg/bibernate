package io.github.blyznytsiaorg.bibernate.onetoone.unidirectional.lazy;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.onetoone.unidirectional.lazy.Address;
import testdata.onetoone.unidirectional.lazy.User;

import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

class FindOneToOneUnidirectionalLazyTest extends AbstractPostgresInfrastructurePrep {
    @DisplayName("Should find exising user by ID with all one to one lazy relations")
    @Test
    void shouldFindUserByIdWithOneToOneLazyRelations() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_USERS_ADDRESSES_HOUSES_TABLES, CREATE_INSERT_USERS_ADRESSES_STATEMENT);
        var persistent = createPersistent("testdata.onetoone.unidirectional.lazy");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                Optional<User> user = bibernateSession.findById(User.class, 1L);

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM users WHERE id = ?;"));
                //then
                assertThat(user).isPresent();

                Address address = user.get().getAddress();
                assertThat(address).isNotNull();
                assertThat(address.getId()).isEqualTo(1L);
                assertThat(address.getName()).isEqualTo("street");

                bibernateSession.findById(Address.class, 1L);

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM users WHERE id = ?;",
                        "SELECT * FROM addresses WHERE id = ?;"));
            }
        }
    }
}
