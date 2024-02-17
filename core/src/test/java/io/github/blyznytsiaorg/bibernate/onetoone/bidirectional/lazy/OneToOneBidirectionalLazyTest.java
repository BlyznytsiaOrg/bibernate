package io.github.blyznytsiaorg.bibernate.onetoone.bidirectional.lazy;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.onetoone.bidirectional.lazy.User;

import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

public class OneToOneBidirectionalLazyTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should retrieve person and address")
    @Test
    public void shouldFindChildUserByIdWithOneToOneLazyBidirectionalRelations() {
        QueryUtils.setupTables(dataSource, CREATE_USERS_ADDRESSES_FOR_BI_TABLES, CREATE_INSERT_USERS_ADRESSES_FOR_BI_STATEMENT);

        var persistent = createPersistent("testdata.onetoone.bidirectional.lazy");
        try (var entityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = entityManager.getBibernateSessionFactory();
            try (var session = bibernateSessionFactory.openSession()) {

                Optional<User> user = session.findById(User.class, 1L);

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * " +
                        "FROM users " +
                        "WHERE users_id = ?;"));
                assertThat(user).isPresent();
                assertThat(user.get())
                        .hasFieldOrPropertyWithValue("id", 1L)
                        .hasFieldOrPropertyWithValue("firstName", "FirstName")
                        .hasFieldOrPropertyWithValue("lastName", "LastName");

                assertThat(user.get().getAddress())
                        .hasFieldOrPropertyWithValue("id", 2L)
                        .hasFieldOrPropertyWithValue("name", "street");

                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM users WHERE users_id = ?;",

                        "SELECT * " +
                        "FROM addresses " +
                        "LEFT JOIN users ON addresses.addresses_id = users.users_address_id " +
                        "WHERE addresses.addresses_id = ?;"));
            }
        }
    }
}