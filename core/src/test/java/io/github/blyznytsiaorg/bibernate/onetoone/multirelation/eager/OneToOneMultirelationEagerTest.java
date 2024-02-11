package io.github.blyznytsiaorg.bibernate.onetoone.multirelation.eager;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.onetoone.multirelation.eager.User;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

class OneToOneMultirelationEagerTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should retrieve user with address and home eager")
    @Test
    void shouldRetrieveUserWithAddressAndHomeEager() {
        QueryUtils.setupTables(dataSource, ONE_TO_ONE_MULTIRELATION_TABLES, ONE_TO_ONE_MULTIRELATION_INSERT);

        var persistent = createPersistent("testdata.onetoone.multirelation.eager");
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
                        "SELECT addresses.id AS addresses_id, addresses.name AS addresses_name, " +
                                "house.id AS house_id, house.name AS house_name, users.id AS users_id, " +
                                "users.first_name AS users_first_name, users.last_name AS users_last_name " +
                                "FROM users " +
                                "LEFT JOIN addresses ON addresses.id = users.address_id " +
                                "LEFT JOIN house ON house.id = addresses.house_id WHERE users.id = ?;"));
            }
        }
    }
}
