package io.github.blyznytsiaorg.bibernate.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchRuntimeException;

import io.github.blyznytsiaorg.bibernate.entity.metadata.EntityMetadataCollector;
import io.github.blyznytsiaorg.bibernate.exception.MappingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EntityMetadataDuplicateTableNameTest {
    private final static String TABLE_NAME = "transport";
    private final static String FIRST_CLASS = "Transport";
    private final static String SECOND_CLASS = "Car";

    @Test
    @DisplayName("should throw exception on duplicate table name")
    void shouldThrowExceptionOnDuplicateTableName() {
        String expectedErrorMessage = "Detected duplicates for table name '%s' in classes '%s', '%s'"
                .formatted(TABLE_NAME, FIRST_CLASS, SECOND_CLASS);

         // when
        EntityMetadataCollector collector = new EntityMetadataCollector("testdata.mappingexception.duplicatetablename");
        RuntimeException exception = catchRuntimeException(collector::collectMetadata);

        String actualMessage = exception.getMessage();

        // then
        assertThat(exception).isExactlyInstanceOf(MappingException.class);
        assertThat(actualMessage).isEqualTo(expectedErrorMessage);
    }


}
