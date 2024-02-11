package io.github.blyznytsiaorg.bibernate.onetoone.multirelation.lazy;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.onetoone.multirelation.lazy.User;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;


class OneToOneMultirelationLazyTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should retrieve user with address and home lazy")
    @Test
    void shouldRetrieveUserWithAddressAndHomeLazy() {
        QueryUtils.setupTables(dataSource, ONE_TO_ONE_MULTIRELATION_TABLES, ONE_TO_ONE_MULTIRELATION_INSERT);

        var persistent = createPersistent("testdata.onetoone.multirelation.lazy");
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = bibernateSessionFactory.openSession()) {

                User user = session.findById(User.class, 1L).orElseThrow();

                assertThat(user).isNotNull();
                assertThat(user.getId()).isEqualTo(1);
                assertThat(user.getLastName()).isEqualTo("Doe");
                assertThat(user.getFirstName()).isEqualTo("John");
                assertThat(user.getAddress()).isNotNull();
                assertThat(user.getAddress().getId()).isEqualTo(1);
                assertThat(user.getAddress().getName()).isEqualTo("Address 1");
                assertThat(user.getAddress().getHouse()).isNotNull();
                assertThat(user.getAddress().getHouse().getId()).isEqualTo(1);
                assertThat(user.getAddress().getHouse().getName()).isEqualTo("House A");

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT users.id as users_id, users.first_name as users_first_name, users.last_name as users_last_name, users.address_id as users_address_id FROM users WHERE users.id = ?;",
                        "SELECT addresses.id as addresses_id, addresses.name as addresses_name, addresses.house_id as addresses_house_id FROM addresses WHERE addresses.id = ?;",
                        "SELECT house.id as house_id, house.name as house_name FROM house WHERE house.id = ?;"));
            }
        }
    }
}
