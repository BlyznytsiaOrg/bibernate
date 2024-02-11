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
    private static final String CLASS_WITH_NO_MANY_TO_MANY = "TestOne";
    private static final String FIELD_WITH_NO_MANY_TO_MANY = "testTwos";
    private static final String FIELD_WITH_NO_MANY_TO_ONE_OR_ONE_TO_ONE = "testTwo";
    private static final String CLASS_WITH_NO_ONE_TO_ONE = "TestTwo";
    private static final String CLASS_WITH_NO_MANY_TO_MANY_ON_RELATION = "TestTwo";
    private static final String CLASS_WITH_ONE_TO_ONE_MAPPED_BY= "TestOne";
    private static final String CLASS_WITH_MANY_TO_MANY_MAPPED_BY= "TestOne";
    private static final String CLASS_WITH_CREATION_TIMESTAMP_AND_UPDATE_TIMESTAMP =  "Test";
    private static final String CLASS_WITH_INSUFFICIENT_TYPE =  "Test";
    private static final String FIELD_WITH_INSUFFICIENT_TYPE =  "time";
    private static final String INSUFFICIENT_TYPE =  "String";
    private static final String MAPPED_BY_ONE_TO_ONE= "testOne";
    private static final String MAPPED_BY_MANY_TO_MANY= "testOnes";
    public static final String CREATED_AT = "createdAt";

    @Test
    @DisplayName("should throw exception on mismatch of index columnList and column name")
    @SneakyThrows
    void shouldThrowExceptionOnIndexMismatchMapping() {
        String expectedErrorMessage = "Error generating index for "
                + "table '%s' [column '%s' does not exist]"
                .formatted(TABLE_NAME, COLUMN_LIST);

        // when
        RuntimeException exception = catchRuntimeException(
                () -> createPersistentWithBb2ddlCreate("testdata.mappingexception.mismatchcolumnlistindex"));
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
        RuntimeException exception = catchRuntimeException(
                () -> createPersistentWithBb2ddlCreate("testdata.mappingexception.notexistedrelation"));
        String actualMessage = exception.getMessage();

        // then
        assertThat(exception).isExactlyInstanceOf(MappingException.class);
        assertThat(actualMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("should throw exception on field with @JoinTable but no @ManyToMany")
    @SneakyThrows
    void shouldThrowExceptionOnNoManyToMany() {
        String expectedErrorMessage = ("No @ManyToMany annotation in class '%s' on field '%s' annotated with annotated @JoinTable")
                .formatted(CLASS_WITH_NO_MANY_TO_MANY, FIELD_WITH_NO_MANY_TO_MANY);

        // when
        RuntimeException exception = catchRuntimeException(
                () -> createPersistentWithBb2ddlCreate("testdata.mappingexception.jointablewithoutmanytomany"));
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
        RuntimeException exception = catchRuntimeException(
                () -> createPersistentWithBb2ddlCreate("testdata.mappingexception.joincolumnmappingexception"));
        String actualMessage = exception.getMessage();

        // then
        assertThat(exception).isExactlyInstanceOf(MappingException.class);
        assertThat(actualMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("should throw exception on @OneToOne annotation with mappedBy that does not have relation")
    @SneakyThrows
    void shouldThrowExceptionOnNoOneToOneMappedBy() {
        String expectedErrorMessage = ("Can't find in entity '%s' @OneToOne annotation "
                +"as entity '%s' is annotated with @OneToOne mappedBy='%s'")
                .formatted(CLASS_WITH_NO_ONE_TO_ONE, CLASS_WITH_ONE_TO_ONE_MAPPED_BY, MAPPED_BY_ONE_TO_ONE);

        // when
        RuntimeException exception = catchRuntimeException(
                () -> createPersistentWithBb2ddlCreate("testdata.mappingexception.onetoonemappedbywithoutrelation"));
        String actualMessage = exception.getMessage();

        // then
        assertThat(exception).isExactlyInstanceOf(MappingException.class);
        assertThat(actualMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("should throw exception on @ManyToMany annotation with mappedBy that does not have relation")
    @SneakyThrows
    void shouldThrowExceptionOnNoManyToManyMappedBy() {
        String expectedErrorMessage = ("Can't find in entity '%s' @ManyToMany annotation "
                +"as entity '%s' is annotated with @ManyToMany mappedBy='%s'")
                .formatted(CLASS_WITH_NO_MANY_TO_MANY_ON_RELATION, CLASS_WITH_MANY_TO_MANY_MAPPED_BY,
                        MAPPED_BY_MANY_TO_MANY);

        // when
        RuntimeException exception = catchRuntimeException(
                () -> createPersistentWithBb2ddlCreate("testdata.mappingexception.manytomanymappedbywithnorelation"));
        String actualMessage = exception.getMessage();

        // then
        assertThat(exception).isExactlyInstanceOf(MappingException.class);
        assertThat(actualMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("should throw exception on @CreationTimestamp and @UpdateTimestamp annotations on one field")
    @SneakyThrows
    void shouldThrowExceptionOnCreationAndUpdateTimestamp() {
        String expectedErrorMessage = ("In class '%s' on field '%s' can't be @CreationTimestamp "
                + "and @UpdateTimestamp annotations simultaneously")
                .formatted(CLASS_WITH_CREATION_TIMESTAMP_AND_UPDATE_TIMESTAMP, CREATED_AT);

        // when
        RuntimeException exception = catchRuntimeException(
                () -> createPersistentWithBb2ddlCreate("testdata.mappingexception.createupdatetimestamp"));
        String actualMessage = exception.getMessage();

        // then
        assertThat(exception).isExactlyInstanceOf(MappingException.class);
        assertThat(actualMessage).isEqualTo(expectedErrorMessage);
    }

    @Test
    @DisplayName("should throw exception on type that is not sufficient for @CreationTimestamp of @UpdateTimestamp annotations")
    @SneakyThrows
    void shouldThrowExceptionOnInsufficientTypeOnTimestamp() {
        String expectedErrorMessage = ("In class '%s' field '%s' with type '%s' is not supported "
                + "for @CreationTimestamp or @UpdateTimestamp annotations")
                .formatted(CLASS_WITH_INSUFFICIENT_TYPE, FIELD_WITH_INSUFFICIENT_TYPE, INSUFFICIENT_TYPE);

        // when
        RuntimeException exception = catchRuntimeException(
                () -> createPersistentWithBb2ddlCreate("testdata.mappingexception.notsufficienttypeontimestamp"));
        String actualMessage = exception.getMessage();

        // then
        assertThat(exception).isExactlyInstanceOf(MappingException.class);
        assertThat(actualMessage).isEqualTo(expectedErrorMessage);
    }
}
