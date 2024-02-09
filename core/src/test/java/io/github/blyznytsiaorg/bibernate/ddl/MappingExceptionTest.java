package io.github.blyznytsiaorg.bibernate.ddl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchRuntimeException;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.exception.MappingException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MappingExceptionTest extends AbstractPostgresInfrastructurePrep {

    private static final String TABLE_NAME = "notes";
    private static final String COLUMN_LIST = "desc";
    private static final String RELATION_CLASS = "NotEntityClass";
    private static final String THIS_CLASS = "EntityClass";
    private static final String CLASS_WITH_NO_MANY_TO_MANY = "TestOne";
    private static final String FIELD_WITH_NO_MANY_TO_MANY = "testTwos";
    private static final String FIELD_WITH_NO_MANY_TO_ONE_OR_ONE_TO_ONE = "testTwo";

    @Test
    @DisplayName("should throw exception on mismatch of index columnList and column name")
    @SneakyThrows
    void shouldThrowExceptionOnIndexMismatchMapping() {
        String expectedErrorMessage = "Error generating index for "
                + "table '%s' [column '%s' does not exist]"
                .formatted(TABLE_NAME, COLUMN_LIST);

        // when
        RuntimeException exception = catchRuntimeException(this::createPersistentIndexColumnListMismatch);
        String actualMessage = exception.getMessage();

        // then
        assertThat(exception).isExactlyInstanceOf(MappingException.class);
        assertThat(actualMessage).isEqualTo(expectedErrorMessage);

    }

    @Test
    @DisplayName("should throw exception on not finding relational entity")
    @SneakyThrows
    void shouldThrowExceptionOnNotFindingRelationalEntity() {
        String expectedErrorMessage = "Can't find @Id annotation in class '%s'"
                .formatted(RELATION_CLASS);

        // when
        RuntimeException exception = catchRuntimeException(this::createPersistentNoRelationFound);
        String actualMessage = exception.getMessage();

        // then
        assertThat(exception).isExactlyInstanceOf(MappingException.class);
        assertThat(actualMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("should throw exception on no @ManyToMany on field annotated with @JoinTable")
    @SneakyThrows
    void shouldThrowExceptionOnNoManyToMany() {
        String expectedErrorMessage = ("No @ManyToMany annotation in class '%s' on field '%s' "
                + "annotated with annotated @JoinTable")
                .formatted(CLASS_WITH_NO_MANY_TO_MANY, FIELD_WITH_NO_MANY_TO_MANY);

        // when
        RuntimeException exception = catchRuntimeException(this::createPersistentNoManyToManyOnJoinTable);
        String actualMessage = exception.getMessage();

        // then
        assertThat(exception).isExactlyInstanceOf(MappingException.class);
        assertThat(actualMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("should throw exception on no @ManyToOne of @ OneToOne on field annotated with @JoinColumn")
    @SneakyThrows
    void shouldThrowExceptionOnNoOneToOneOrManyToOne() {
        String expectedErrorMessage = ("No @OneToOne or @ManyToOne annotation on field '%s' "
                + "annotated with @JoinColumn")
                .formatted(FIELD_WITH_NO_MANY_TO_ONE_OR_ONE_TO_ONE);

        // when
        RuntimeException exception = catchRuntimeException(this::createPersistentJoinColumnMappingException);
        String actualMessage = exception.getMessage();

        // then
        assertThat(exception).isExactlyInstanceOf(MappingException.class);
        assertThat(actualMessage).isEqualTo(expectedErrorMessage);
    }
}
