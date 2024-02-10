package io.github.blyznytsiaorg.bibernate.onetoone.unidirectional.eager;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.onetoone.unidirectional.eager.Address;
import testdata.onetoone.unidirectional.eager.House;
import testdata.onetoone.unidirectional.eager.User;

import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

class FindOneToOneUnidirectionalEagerTest extends AbstractPostgresInfrastructurePrep {
    @DisplayName("Should find exising person by ID with all one to one relations")
    @Test
    void shouldFindUserByIdWith2OneToOneEagerRelations() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_USERS_ADDRESSES_HOUSES_TABLES, CREATE_INSERT_USERS_ADRESSES_STATEMENT);
        var persistent = createPersistent("testdata.onetoone.unidirectional.eager");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                Optional<User> user = bibernateSession.findById(User.class, 1L);

                //then
                assertThat(user).isPresent();

                Address address = user.get().getAddress();
                assertThat(address).isNotNull();
                assertThat(address.getId()).isEqualTo(1L);
                assertThat(address.getName()).isEqualTo("street");

                House house = user.get().getHouse();
                assertThat(house).isNotNull();
                assertThat(house.getId()).isEqualTo(2);
                assertThat(house.getName()).isEqualTo("big");

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT addresses.id AS addresses_id, " +
                        "addresses.name AS addresses_name, " +
                        "houses.id AS houses_id, " +
                        "houses.name AS houses_name, " +
                        "users.id AS users_id, " +
                        "users.first_name AS users_first_name, " +
                        "users.last_name AS users_last_name " +
                        "FROM users " +
                        "LEFT JOIN addresses ON addresses.id = users.address_id " +
                        "LEFT JOIN houses ON houses.id = users.house_id " +
                        "WHERE users.id = ?;"));
            }
        }
    }
}
