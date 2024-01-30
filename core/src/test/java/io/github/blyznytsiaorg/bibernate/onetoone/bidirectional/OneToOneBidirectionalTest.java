package io.github.blyznytsiaorg.bibernate.onetoone.bidirectional;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.onetoone.bidirectional.Address;

import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;


public class OneToOneBidirectionalTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should retrieve person and address")
    @Test
    public void testOneToOneBidirectional() {
        QueryUtils.setupTables(dataSource, CREATE_USERS_ADDRESSES_HOUSES_TABLES, CREATE_INSERT_USERS_ADRESSES_STATEMENT);

        var persistent = createPersistent();
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var sessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = sessionFactory.openSession()) {

                Optional<Address> address = session.findById(Address.class, 1L);

                assertThat(address).isPresent();
                assertThat(address.get())
                        .hasFieldOrPropertyWithValue("id", 1L)
                        .hasFieldOrPropertyWithValue("name", "street");

                assertThat(address.get().getUser())
                        .hasFieldOrPropertyWithValue("id", 1L)
                        .hasFieldOrPropertyWithValue("firstName", "FirstName")
                        .hasFieldOrPropertyWithValue("lastName", "LastName");

                assertQueries(sessionFactory, List.of(
                        "SELECT * FROM addresses WHERE id = ?;",
                        "SELECT * FROM users WHERE address_id = ?;",
                        "SELECT * FROM houses WHERE house_id = ?;"));


            }
        }
    }
}
