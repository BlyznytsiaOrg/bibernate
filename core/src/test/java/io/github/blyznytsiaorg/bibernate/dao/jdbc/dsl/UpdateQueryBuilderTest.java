package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateQueryBuilderTest {

    @DisplayName("should build update query")
    @Test
    void shouldBuildUpdateQuery() {
        String query = UpdateQueryBuilder.update("users")
                .setField("age", "?")
                .whereCondition("id = ?")
                .buildUpdateStatement();

        assertThat(query).isEqualTo("UPDATE users SET age = ? WHERE id = ?;");
    }
}