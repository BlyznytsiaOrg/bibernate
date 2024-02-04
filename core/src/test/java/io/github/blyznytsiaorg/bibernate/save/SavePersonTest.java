package io.github.blyznytsiaorg.bibernate.save;


import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.simplerespository.Person;

import static org.assertj.core.api.Assertions.assertThat;

class SavePersonTest extends AbstractPostgresInfrastructurePrep {

    @Test
    @DisplayName("Should save person")
    void shouldSavePerson() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent();

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var person = new Person();
                person.setId(2L);
                person.setFirstName("Rake");
                person.setLastName("Tell");

                //when
                var savedPerson = bibernateSession.save(Person.class, person);
                bibernateSession.flush();

                //then
                assertThat(savedPerson).isNotNull();
                assertThat(savedPerson.getId()).isNotNull();
                assertThat(savedPerson.getFirstName()).isEqualTo(person.getFirstName());
                assertThat(savedPerson.getLastName()).isEqualTo(person.getLastName());
            }
        }
    }
}
