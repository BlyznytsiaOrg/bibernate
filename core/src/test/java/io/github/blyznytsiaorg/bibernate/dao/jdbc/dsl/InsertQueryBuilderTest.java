package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InsertQueryBuilderTest {


    @DisplayName("Should build insert")
    @Test
    void shouldBuildInsert() {
        var insertQueryBuilder = InsertQueryBuilder.from("users")
                .setField("name")
                .setField("age");

        String query = insertQueryBuilder.buildInsertStatement();

        assertThat(query).isEqualTo("INSERT INTO users ( name, age ) VALUES ( ?, ? );");
    }

}