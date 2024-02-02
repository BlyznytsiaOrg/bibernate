package io.github.blyznytsiaorg.bibernate.save;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.update.EmployeeEntity;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

class OptimisticVersionUserTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should save new employee and set version to one")
    @Test
    void shouldSaveNewEmployeeAndSetVersionTwoOne() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_EMPLOYEE_TABLE, CREATE_EMPLOYEE_INSERT_STATEMENT);

        var persistent = createPersistent();
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                var employeeEntity = new EmployeeEntity();
                employeeEntity.setId(2L);
                employeeEntity.setFirstName("FirstName2");
                employeeEntity.setLastName("LastName2");

                var saveEmployeeEntity = bibernateSession.save(EmployeeEntity.class, employeeEntity);

                //then
                assertThat(saveEmployeeEntity).isNotNull();
                assertThat(saveEmployeeEntity.getId()).isEqualTo(2L);
                assertThat(saveEmployeeEntity.getFirstName()).isEqualTo("FirstName2");
                assertThat(saveEmployeeEntity.getLastName()).isEqualTo("LastName2");
                assertThat(saveEmployeeEntity.getVersion()).isEqualTo(1);

                assertQueries(bibernateSessionFactory, List.of("INSERT INTO employees ( id, first_name, last_name, version ) VALUES ( ?, ?, ?, ? );"));
            }
        }
    }
}
