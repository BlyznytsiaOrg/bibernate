package io.github.blyznytsiaorg.bibernate.save;


import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.simplerespository.Person;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

class SavePersonTest extends AbstractPostgresInfrastructurePrep {

    @Test
    @DisplayName("Should save person")
    public void shouldSavePerson() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var person = new Person();
                person.setFirstName("Rake");
                person.setLastName("Tell");

                //when
                var savedPerson = bibernateSession.save(Person.class, person);

                //then
                assertThat(savedPerson).isNotNull();
                // TODO uncomment when GeneratedValue is done
//                assertThat(savedPerson.getId()).isNotNull();
                assertThat(savedPerson.getFirstName()).isEqualTo(person.getFirstName());
                assertThat(savedPerson.getLastName()).isEqualTo(person.getLastName());
            }

            //then
            assertQueries(bibernateSessionFactory, List.of(
                    "INSERT INTO persons ( first_name, last_name ) VALUES ( ?, ? );"));
        }
    }
}
