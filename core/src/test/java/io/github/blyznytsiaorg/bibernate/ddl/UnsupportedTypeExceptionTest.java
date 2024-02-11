package io.github.blyznytsiaorg.bibernate.ddl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchRuntimeException;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.exception.UnsupportedDataTypeException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UnsupportedTypeExceptionTest extends AbstractPostgresInfrastructurePrep {

    private static final String TABLE_WITH_UNSUPPORTED_TYPE_FOR_ID_GENERATION =  "test";
    private static final String TABLE_WITH_UNSUPPORTED_TYPE_FOR_SEQUENCE =  "test";
    private static final String TABLE_WITH_UNSUPPORTED_TYPE_FOR_DDL =  "test";
    private static final String UNSUPPORTED_TYPE_FOR_ID =  "String";
    private static final String UNSUPPORTED_TYPE_FOR_SEQUENCE =  "LocalDateTime";
    private static final String UNSUPPORTED_TYPE_RANDOM =  "Random";

    @Test
    @DisplayName("should throw exception on unsupported type for @GeneratedValue strategy IDENTITY")
    @SneakyThrows
    void shouldThrowExceptionOnUnSupportedTypeForIdGeneration() {
        String expectedErrorMessage = ("Error creating SQL commands for DDL creation for "
                + "table '%s' [illegal identity column type '%s' for id generation]")
                .formatted(TABLE_WITH_UNSUPPORTED_TYPE_FOR_ID_GENERATION, UNSUPPORTED_TYPE_FOR_ID);

        // when
        RuntimeException exception = catchRuntimeException(
                () -> createPersistentWithBb2ddlCreate("testdata.unsupportedtype.idgeneration"));
        String actualMessage = exception.getMessage();

        // then
        assertThat(exception).isExactlyInstanceOf(UnsupportedDataTypeException.class);
        assertThat(actualMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("should throw exception on unsupported type for @GeneratedValue strategy SEQUENCE")
    @SneakyThrows
    void shouldThrowExceptionOnUnSupportedTypeForSequence() {
        String expectedErrorMessage = ("Error creating SQL commands for DDL creation for "
                + "table '%s' [illegal identity column type '%s' for id generation]")
                .formatted(TABLE_WITH_UNSUPPORTED_TYPE_FOR_SEQUENCE, UNSUPPORTED_TYPE_FOR_SEQUENCE);

        // when
        RuntimeException exception = catchRuntimeException(
                () -> createPersistentWithBb2ddlCreate("testdata.unsupportedtype.sequnce"));
        String actualMessage = exception.getMessage();

        // then
        assertThat(exception).isExactlyInstanceOf(UnsupportedDataTypeException.class);
        assertThat(actualMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("should throw exception on unsupported type for DDL creation")
    @SneakyThrows
    void shouldThrowExceptionOnUnSupportedTypeForDDLCreation() {
        String expectedErrorMessage = ("Error creating SQL commands for DDL creation "
                + "for table '%s' [column type '%s' is not supported]")
                .formatted(TABLE_WITH_UNSUPPORTED_TYPE_FOR_DDL, UNSUPPORTED_TYPE_RANDOM);

        // when
        RuntimeException exception = catchRuntimeException(
                () -> createPersistentWithBb2ddlCreate("testdata.unsupportedtype.regulartype"));
        String actualMessage = exception.getMessage();

        // then
        assertThat(exception).isExactlyInstanceOf(UnsupportedDataTypeException.class);
        assertThat(actualMessage).isEqualTo(expectedErrorMessage);
    }
}
