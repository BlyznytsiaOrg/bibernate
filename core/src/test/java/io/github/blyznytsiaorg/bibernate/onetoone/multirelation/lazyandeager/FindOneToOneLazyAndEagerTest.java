package io.github.blyznytsiaorg.bibernate.onetoone.multirelation.lazyandeager;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.onetoone.multirelation.find.lazyandeager.User;

import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

public class FindOneToOneLazyAndEagerTest extends AbstractPostgresInfrastructurePrep {
    @DisplayName("Should find exising user by ID with all one to one lazy and eager relations")
    @Test
    void shouldFindUserByIdWithOneToOneLazyAndEagerRelations() {
        //given
        QueryUtils.setupTables(dataSource, ONE_TO_ONE_EAGER_AND_LAZY_TABLES, ONE_TO_ONE_EAGER_AND_LAZY_INSERT);
        var persistent = createPersistent("testdata.onetoone.multirelation.find.lazyandeager");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                Optional<User> user = bibernateSession.findById(User.class, 1L);

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * " +
                        "FROM users " +
                        "LEFT JOIN addresses ON addresses.addresses_id = users.users_address_id " +
                        "LEFT JOIN houses ON houses.houses_id = addresses.addresses_house_id " +
                        "WHERE users.users_id = ?;"));
                //then
                assertThat(user).isPresent();

                var address = user.get().getAddress();
                assertThat(address).isNotNull();
                assertThat(address.getId()).isEqualTo(2L);
                assertThat(address.getName()).isEqualTo("Address 1");

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * " +
                        "FROM users " +
                        "LEFT JOIN addresses ON addresses.addresses_id = users.users_address_id " +
                        "LEFT JOIN houses ON houses.houses_id = addresses.addresses_house_id " +
                        "WHERE users.users_id = ?;"));

                var profile = user.get().getProfile();
                assertThat(profile).isNotNull();
                assertThat(profile.getNickname()).isEqualTo("nickname");

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * " +
                        "FROM users " +
                        "LEFT JOIN addresses ON addresses.addresses_id = users.users_address_id " +
                        "LEFT JOIN houses ON houses.houses_id = addresses.addresses_house_id " +
                        "WHERE users.users_id = ?;",
                        "SELECT * FROM profiles WHERE profiles_id = ?;"));
            }
        }
    }
}
