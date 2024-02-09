package io.github.blyznytsiaorg.bibernate.sessionisclosed;


import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.exception.BibernateSessionClosedException;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.simplerespository.Person;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SessionIsClosedTest extends AbstractPostgresInfrastructurePrep {

    @Test
    @DisplayName("Should throw exception when session is closed")
    void shouldThrowExceptionWhenSessionIsClosed() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent("testdata.simplerespository");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            var bibernateSession = bibernateSessionFactory.openSession();
            bibernateSession.close();

            //when
            assertThrows(BibernateSessionClosedException.class,
                    () -> bibernateSession.deleteById(Person.class, 1L));

            //then
            assertThat(bibernateSessionFactory.getExecutedQueries()).isEmpty();
        }
    }
}
