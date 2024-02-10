package io.github.blyznytsiaorg.bibernate.onetoone.bidirectional.eager;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.onetoone.bidirectional.eager.Address;

import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;


public class OneToOneBidirectionalEagerTest extends AbstractPostgresInfrastructurePrep {

    @Disabled
    @DisplayName("Should retrieve person and address")
    @Test
    public void shouldFindUserByIdWithOneToOneEagerBidirectionalRelations() {
        QueryUtils.setupTables(dataSource, CREATE_USERS_ADDRESSES_FOR_BI_TABLES, CREATE_INSERT_USERS_ADRESSES_FOR_BI_STATEMENT);

        var persistent = createPersistent("testdata.onetoone.bidirectional.eager");
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = bibernateSessionFactory.openSession()) {

                Optional<Address> address = session.findById(Address.class, 2L);

                assertThat(address).isPresent();
                assertThat(address.get())
                        .hasFieldOrPropertyWithValue("id", 1L)
                        .hasFieldOrPropertyWithValue("name", "street");

                assertThat(address.get().getUser())
                        .hasFieldOrPropertyWithValue("id", 1L)
                        .hasFieldOrPropertyWithValue("firstName", "FirstName")
                        .hasFieldOrPropertyWithValue("lastName", "LastName");

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT addresses.id AS addresses_id, " +
                        "addresses.name AS addresses_name, " +
                        "users.id AS users_id, " +
                        "users.first_name AS users_first_name, " +
                        "users.last_name AS users_last_name, " +
                        "FROM addresses LEFT JOIN users ON users.id=addresses.id " +
                        "WHERE addresses.id = ?;"));
            }
        }
    }
}
