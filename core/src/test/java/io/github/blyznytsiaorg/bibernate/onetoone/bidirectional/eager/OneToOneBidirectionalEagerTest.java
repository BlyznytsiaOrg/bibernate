package io.github.blyznytsiaorg.bibernate.onetoone.bidirectional.eager;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.onetoone.bidirectional.eager.Address;
import testdata.onetoone.bidirectional.eager.User;

import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;


public class OneToOneBidirectionalEagerTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should retrieve person and address")
    @Test
    public void shouldFindUserByIdWithOneToOneEagerBidirectionalRelationsOnOwnerSide() {
        QueryUtils.setupTables(dataSource, CREATE_USERS_ADDRESSES_FOR_BI_TABLES, CREATE_INSERT_USERS_ADRESSES_FOR_BI_STATEMENT);

        var persistent = createPersistent("testdata.onetoone.bidirectional.eager");
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = bibernateSessionFactory.openSession()) {

                Optional<Address> address = session.findById(Address.class, 2L);

                assertThat(address).isPresent();
                assertThat(address.get())
                        .hasFieldOrPropertyWithValue("id", 2L)
                        .hasFieldOrPropertyWithValue("name", "street");

                assertThat(address.get().getUser())
                        .hasFieldOrPropertyWithValue("id", 1L)
                        .hasFieldOrPropertyWithValue("firstName", "FirstName")
                        .hasFieldOrPropertyWithValue("lastName", "LastName");

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * " +
                        "FROM addresses " +
                        "LEFT JOIN users ON addresses.addresses_id = users.users_address_id " +
                        "WHERE addresses.addresses_id = ?;"));
            }
        }
    }

    @DisplayName("Should retrieve person and address")
    @Test
    public void shouldFindUserByIdWithOneToOneEagerBidirectionalRelations() {
        QueryUtils.setupTables(dataSource, CREATE_USERS_ADDRESSES_FOR_BI_TABLES, CREATE_INSERT_USERS_ADRESSES_FOR_BI_STATEMENT);

        var persistent = createPersistent("testdata.onetoone.bidirectional.eager");
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = bibernateSessionFactory.openSession()) {

                Optional<User> user = session.findById(User.class, 1L);

                assertThat(user).isPresent();

                assertThat(user.get())
                        .hasFieldOrPropertyWithValue("id", 1L)
                        .hasFieldOrPropertyWithValue("firstName", "FirstName")
                        .hasFieldOrPropertyWithValue("lastName", "LastName");

                assertThat(user.get().getAddress())
                        .hasFieldOrPropertyWithValue("id", 2L)
                        .hasFieldOrPropertyWithValue("name", "street");

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * " +
                        "FROM users " +
                        "LEFT JOIN addresses ON addresses.addresses_id = users.users_address_id " +
                        "WHERE users.users_id = ?;"));
            }
        }
    }
}
