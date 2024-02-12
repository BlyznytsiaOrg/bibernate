package io.github.blyznytsiaorg.bibernate.ddl;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.Persistent;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import testdata.timestamp.Customer;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

class TimestampTest extends AbstractPostgresInfrastructurePrep {

    @Test
    @DisplayName("should set time to field annotated with @CreationTimestamp and update time for field annotated to @UpdateTimestamp")
    @SneakyThrows
    void shouldCreateUpdateTimestamp() {
        //given
        Persistent persistent = createPersistentWithBb2ddlCreate("testdata.timestamp");

        OffsetDateTime updatedAtFirst;
        LocalDateTime createdAtFirst;
        OffsetDateTime updatedAtSecond;
        LocalDateTime createdAtSecond;
        OffsetDateTime updatedAtThird;
        LocalDateTime createdAtThird;

        //when
        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            try (var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory()) {
                try (var bibernateSession = bibernateSessionFactory.openSession()) {


                    Customer customer = new Customer();
                    customer.setName("First");
                    bibernateSession.save(Customer.class, customer);
                }

                Thread.sleep(1000);

                try (var bibernateSession = bibernateSessionFactory.openSession()) {

                    Customer savedCustomer = bibernateSession.findById(Customer.class, 1L).orElseThrow();
                    updatedAtFirst = savedCustomer.getUpdatedAt();
                    createdAtFirst = savedCustomer.getCreatedAt();

                    savedCustomer.setName("Second");
                }

                Thread.sleep(1000);

                try (var bibernateSession = bibernateSessionFactory.openSession()) {

                    Customer savedCustomer = bibernateSession.findById(Customer.class, 1L).orElseThrow();
                    updatedAtSecond = savedCustomer.getUpdatedAt();
                    createdAtSecond = savedCustomer.getCreatedAt();

                    savedCustomer.setName("Third");
                }

                Thread.sleep(1000);

                try (var bibernateSession = bibernateSessionFactory.openSession()) {

                    Customer savedCustomer = bibernateSession.findById(Customer.class, 1L).orElseThrow();
                    updatedAtThird = savedCustomer.getUpdatedAt();
                    createdAtThird = savedCustomer.getCreatedAt();
                }

                //then
                assertThat(updatedAtFirst).isNull();
                assertThat(createdAtFirst).isNotNull();
                assertThat(createdAtFirst).isEqualTo(createdAtSecond).isEqualTo(createdAtThird);
                assertThat(updatedAtSecond).isNotNull();
                assertThat(updatedAtThird).isAfter(updatedAtSecond);
            }
        }
    }
}
