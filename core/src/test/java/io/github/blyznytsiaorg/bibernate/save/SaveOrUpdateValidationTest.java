package io.github.blyznytsiaorg.bibernate.save;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.exception.BibernateValidationException;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import testdata.simplerespository.Person;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SaveOrUpdateValidationTest extends AbstractPostgresInfrastructurePrep  {

    @Test
    @DisplayName("Should throw exception Id value not set for person on save")
    void shouldThrowExceptionIdValueNotSetForPersonOnSave() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent("testdata.simplerespository");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var person = new testdata.simplerespository.Person();
                person.setFirstName("Rake");
                person.setLastName("Tell");

                //when
                Executable executable = () -> {
                    var savedPerson = bibernateSession.save(testdata.simplerespository.Person.class, person);
                };

                // then
                var noSuchBeanException = assertThrows(BibernateValidationException.class, executable);
                assertThat(noSuchBeanException.getMessage()).isEqualTo("Entity Person should have Id that not null or add annotation @GeneratedValue");
            }
        }
    }

    @Test
    @DisplayName("Should not throw exception Id value not set for person has generatorValue annotation on save")
    void shouldNotThrowExceptionIdValueNotSetForPersonHasGeneratorValueOnSave() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_PERSONS_TABLE, CREATE_PERSONS_INSERT_STATEMENT);
        var persistent = createPersistent("testdata.save");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var person = new testdata.save.Person();
                person.setFirstName("Rake");
                person.setLastName("Tell");

                //when
                var savedPerson = bibernateSession.save(testdata.save.Person.class, person);

                //then
                assertThat(savedPerson).isNotNull();
            }

            assertQueries(bibernateSessionFactory, List.of(
                    "INSERT INTO persons ( first_name, last_name ) VALUES ( ?, ? );"));
        }
    }
}
