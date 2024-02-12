package io.github.blyznytsiaorg.bibernate.onetoone.multirelation.eager;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.onetoone.multirelation.eager.Address;
import testdata.onetoone.multirelation.eager.House;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

public class OneToOneMultirelationEagerTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should retrieve user with address and home eager")
    @Test
    void shouldRetrieveUserWithAddressAndHomeEager() {
        var persistent = createPersistentWithBb2ddlCreate("testdata.onetoone.multirelation.eager");
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = bibernateSessionFactory.openSession()) {

                var house = new House();
                house.setName("House name 1");

                House saveHouse = session.save(House.class, house);

                var address = new Address();
                address.setName("Name 1");
                address.setHouse(saveHouse);

                Address savedAddress = session.save(Address.class, address);
                session.flush();

                assertThat(savedAddress.getId()).isNotNull();
                assertThat(savedAddress.getId()).isEqualTo(1L);
                assertThat(savedAddress.getHouse().getId()).isEqualTo(1L);

                assertQueries(bibernateSessionFactory, List.of("INSERT INTO houses ( name ) VALUES ( ? );",
                        "INSERT INTO addresses ( name, house_id ) VALUES ( ?, ? );"));

            }
        }
    }
}
