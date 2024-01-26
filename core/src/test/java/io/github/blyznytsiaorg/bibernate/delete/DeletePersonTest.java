package io.github.blyznytsiaorg.bibernate.delete;


import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.simplerespository.Person;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;

class DeletePersonTest extends AbstractPostgresInfrastructurePrep {

    @Test
    @DisplayName("Should delete person")
    void shouldSavePerson() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent();

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                //when
                bibernateSession.delete(Person.class, 1L);

                //then
                assertQueries(bibernateSessionFactory, List.of("DELETE FROM persons WHERE id = ?;"));
            }
        }
    }
}
