package io.github.blyznytsiaorg.bibernate.update;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import testdata.update.EmployeeEntity;

import java.util.List;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OptimisticVersionUserTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should update user with version field")
    @Test
    void shouldUpdateEmployeeWithVersion() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_EMPLOYEE_TABLE, CREATE_EMPLOYEE_INSERT_STATEMENT);

        var persistent = createPersistent("");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                EmployeeEntity employeeEntity = bibernateSession.findById(EmployeeEntity.class, 10L).orElseThrow();

                employeeEntity.setLastName("Update " + employeeEntity.getLastName());
            }

            //then
            assertQueries(bibernateSessionFactory, List.of(
                    "SELECT * FROM employees WHERE id = ?;",
                    "UPDATE employees SET first_name = ?, last_name = ?, version = version + 1 WHERE id = ? AND version = ?;")
            );

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                EmployeeEntity employeeEntity = bibernateSession.findById(EmployeeEntity.class, 10L).orElseThrow();

                //then
                assertThat(employeeEntity.getVersion()).isEqualTo(2);
            }

            //then
            assertQueries(bibernateSessionFactory, List.of("SELECT * FROM employees WHERE id = ?;"));
        }
    }

    @DisplayName("Should throw optimistic exception when version that I use not match with database")
    @Test
    void shouldThrowOptimisticExceptionWhenVersionThatIUseNotMatchWithDatabase() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_EMPLOYEE_TABLE, CREATE_EMPLOYEE_INSERT_STATEMENT);

        var persistent = createPersistent("");

        Executable executable = () -> {
            try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
                var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

                try (var bibernateSession = bibernateSessionFactory.openSession()) {
                    //when
                    EmployeeEntity employeeEntity = bibernateSession.findById(EmployeeEntity.class, 10L).orElseThrow();

                    assertQueries(bibernateSessionFactory, List.of(
                            "SELECT * FROM employees WHERE id = ?;"
                    ));

                    employeeEntity.setVersion(3);
                    employeeEntity.setLastName("Update " + employeeEntity.getLastName());
                }
            }
        };

        // then

        var entityStateWasChangeException = assertThrows(BibernateGeneralException.class, executable);
        assertThat(entityStateWasChangeException.getCause().getMessage())
                .isEqualTo("Entity class testdata.update.EmployeeEntity was change need to get new data findByid[10]");

    }
}
