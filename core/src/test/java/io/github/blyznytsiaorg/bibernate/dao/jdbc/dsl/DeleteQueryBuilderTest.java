package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeleteQueryBuilderTest {

    @DisplayName("should build delete query")
    @Test
    void shouldBuildUpdateQuery() {
        var query = DeleteQueryBuilder.from("users")
                .whereCondition("age > ?")
                .andCondition("enabled = ?")
                .buildDeleteStatement();

        assertThat(query).isEqualTo("DELETE FROM users WHERE age > ? AND enabled = ?;");
    }

}