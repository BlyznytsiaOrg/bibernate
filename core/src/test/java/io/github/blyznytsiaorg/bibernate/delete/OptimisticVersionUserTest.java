package io.github.blyznytsiaorg.bibernate.delete;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.update.optimistic.EmployeeEntity;

import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.setupTables;
import static org.assertj.core.api.Assertions.assertThat;

class OptimisticVersionUserTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should skip delete if version not match")
    @Test
    void shouldSkipDeleteIfVersionNotMatch() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_EMPLOYEE_TABLE, CREATE_EMPLOYEE_INSERT_STATEMENT);

        var persistent = createPersistent("testdata.update");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            EmployeeEntity employeeEntity;

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                employeeEntity = bibernateSession.findById(EmployeeEntity.class, 10L).orElseThrow();

                //then
                assertQueries(bibernateSessionFactory, List.of("SELECT * FROM employees WHERE id = ?;"));
            }

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                employeeEntity.setVersion(2);
                bibernateSession.delete(EmployeeEntity.class, employeeEntity);
                bibernateSession.flush();

                //then
                assertQueries(bibernateSessionFactory, List.of("DELETE FROM employees WHERE id = ? AND version = ?;"));
            }

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                employeeEntity = bibernateSession.findById(EmployeeEntity.class, 10L).orElseThrow();

                //then
                assertThat(employeeEntity).isNotNull();
                assertQueries(bibernateSessionFactory, List.of("SELECT * FROM employees WHERE id = ?;"));
            }
        }
    }

    @DisplayName("Should skip delete all if version not match")
    @Test
    void shouldSkipDeleteAllIfVersionNotMatch() {
        //given
        createTableWithData(3);
        var persistent = createPersistent();

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var employees = bibernateSession.findAll(EmployeeEntity.class);
                employees.forEach(employee -> employee.setVersion(2));

                //when
                bibernateSession.deleteAll(EmployeeEntity.class, employees);

                //then
                var removedEmployees = bibernateSession.findAll(EmployeeEntity.class);

                assertThat(removedEmployees.size()).isEqualTo(employees.size());
                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM employees;",
                        "DELETE FROM employees WHERE id = ? AND version = ?;",
                        "DELETE FROM employees WHERE id = ? AND version = ?;",
                        "DELETE FROM employees WHERE id = ? AND version = ?;",
                        "SELECT * FROM employees;"));
            }
        }
    }

    @DisplayName("Should delete if version match")
    @Test
    void shouldDeleteIfVersionMatch() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_EMPLOYEE_TABLE, CREATE_EMPLOYEE_INSERT_STATEMENT);

        var persistent = createPersistent("testdata.update");
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            EmployeeEntity employeeEntity;

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                employeeEntity = bibernateSession.findById(EmployeeEntity.class, 10L).orElseThrow();

                //then
                assertQueries(bibernateSessionFactory, List.of("SELECT * FROM employees WHERE id = ?;"));
            }

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                bibernateSession.delete(EmployeeEntity.class, employeeEntity);
                bibernateSession.flush();

                //then
                assertQueries(bibernateSessionFactory, List.of("DELETE FROM employees WHERE id = ? AND version = ?;"));
            }

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                Optional<EmployeeEntity> emp = bibernateSession.findById(EmployeeEntity.class, 10L);

                //then
                assertThat(emp).isEmpty();
                assertQueries(bibernateSessionFactory, List.of("SELECT * FROM employees WHERE id = ?;"));
            }
        }
    }

    @DisplayName("Should delete all if version match")
    @Test
    void shouldDeleteAllIfVersionMatch() {
        //given
        createTableWithData(3);
        var persistent = createPersistent();

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {

                var employees = bibernateSession.findAll(EmployeeEntity.class);

                //when
                bibernateSession.deleteAll(EmployeeEntity.class, employees);

                //then
                var removedEmployees = bibernateSession.findAll(EmployeeEntity.class);

                assertThat(removedEmployees).isEmpty();
                assertQueries(bibernateSessionFactory, List.of(
                        "SELECT * FROM employees;",
                        "DELETE FROM employees WHERE id = ? AND version = ?;",
                        "DELETE FROM employees WHERE id = ? AND version = ?;",
                        "DELETE FROM employees WHERE id = ? AND version = ?;",
                        "SELECT * FROM employees;"));
            }
        }
    }

    private void createTableWithData(int i) {
        setupTables(dataSource, CREATE_EMPLOYEE_TABLE, CREATE_EMPLOYEE_GENERAL_INSERT_STATEMENT.formatted("John" + i, "Doe" + i, 1));
        setupTables(dataSource, CREATE_EMPLOYEE_TABLE, CREATE_EMPLOYEE_GENERAL_INSERT_STATEMENT.formatted("Jane" + i, "Smith" + i, 1));
        setupTables(dataSource, CREATE_EMPLOYEE_TABLE, CREATE_EMPLOYEE_GENERAL_INSERT_STATEMENT.formatted("John" + i, "Smith" + i, 1));
    }
}
