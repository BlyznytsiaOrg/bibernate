package io.github.blyznytsiaorg.bibernate.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.exception.BibernateGeneralException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class BibernateConfigurationTest extends AbstractPostgresInfrastructurePrep {

    @Test
    @DisplayName("should throw exception on flyway enabled and ddl create")
    void shouldThrowExceptionOnFlywayEnabledAndDDLCreate() {
        String expectedMessage = "Configuration error: bibernate.flyway.enabled=true "
                + "and bibernate.2ddl.auto=create. Choose one property for creating schema";

        // when
        RuntimeException exception = Assertions.catchRuntimeException(() -> createPersistentWithFlayWayEnabledAndBb2ddlCreate("testdata.entity"));
        String message = exception.getMessage();

        //then
        assertThat(exception).isInstanceOf(BibernateGeneralException.class);
        assertThat(message).isEqualTo(expectedMessage);
    }
}