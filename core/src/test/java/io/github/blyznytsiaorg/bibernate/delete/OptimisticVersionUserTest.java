package io.github.blyznytsiaorg.bibernate.delete;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.utils.QueryUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.update.EmployeeEntity;

import java.util.List;
import java.util.Optional;

import static io.github.blyznytsiaorg.bibernate.utils.QueryUtils.assertQueries;
import static org.assertj.core.api.Assertions.assertThat;

class OptimisticVersionUserTest extends AbstractPostgresInfrastructurePrep {

    @DisplayName("Should skip delete if version not match")
    @Test
    void shouldSkipDeleteIfVersionNotMatch() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_EMPLOYEE_TABLE, CREATE_EMPLOYEE_INSERT_STATEMENT);

        var persistent = createPersistent();
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            EmployeeEntity employeeEntity;

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                employeeEntity = bibernateSession.findById(EmployeeEntity.class, 1L).orElseThrow();

                //then
                assertQueries(bibernateSessionFactory, List.of("SELECT * FROM employees WHERE id = ?;"));
            }

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                employeeEntity.setVersion(2);
                bibernateSession.delete(EmployeeEntity.class, employeeEntity);

                //then
                assertQueries(bibernateSessionFactory, List.of("DELETE FROM employees WHERE id = ? AND version = ?;"));
            }

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                employeeEntity = bibernateSession.findById(EmployeeEntity.class, 1L).orElseThrow();

                //then
                assertThat(employeeEntity).isNotNull();
                assertQueries(bibernateSessionFactory, List.of("SELECT * FROM employees WHERE id = ?;"));
            }
        }
    }

    @DisplayName("Should skip delete if version match")
    @Test
    void shouldSkipDeleteIfVersionMatch() {
        //given
        QueryUtils.setupTables(dataSource, CREATE_EMPLOYEE_TABLE, CREATE_EMPLOYEE_INSERT_STATEMENT);

        var persistent = createPersistent();
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();

            EmployeeEntity employeeEntity;

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                employeeEntity = bibernateSession.findById(EmployeeEntity.class, 1L).orElseThrow();

                //then
                assertQueries(bibernateSessionFactory, List.of("SELECT * FROM employees WHERE id = ?;"));
            }

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                bibernateSession.delete(EmployeeEntity.class, employeeEntity);

                //then
                assertQueries(bibernateSessionFactory, List.of("DELETE FROM employees WHERE id = ? AND version = ?;"));
            }

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                Optional<EmployeeEntity> emp = bibernateSession.findById(EmployeeEntity.class, 1L);

                //then
                assertThat(emp).isEmpty();
                assertQueries(bibernateSessionFactory, List.of("SELECT * FROM employees WHERE id = ?;"));
            }
        }
    }
}
