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
    @DisplayName("Should find exising person by ID with all one to one relations")
    @Disabled
    @Test
    void shouldFindExistingPersonByIdWithRelations() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_USERS_ADDRESSES_HOUSES_TABLES, CREATE_INSERT_USERS_ADRESSES_STATEMENT);
        var persistent = createPersistent();

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
//                House house = address.getHouse();
//                assertThat(house).isNotNull();
//                assertThat(house.getId()).isEqualTo(1L);
//                assertThat(house.getName()).isEqualTo("big");
            }

            //then
//            assertQueries(bibernateSessionFactory, List.of(
//                    "SELECT users.id AS users_id, users.first_name AS users_first_name, users.last_name AS users_last_name, addresses.id AS addresses_id, addresses.name AS addresses_name FROM users LEFT JOIN addresses ON users.id=addresses.id WHERE users.id = ?;"));
        }
    }
}
