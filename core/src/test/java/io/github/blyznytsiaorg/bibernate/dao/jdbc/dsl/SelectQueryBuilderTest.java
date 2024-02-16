package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.join.JoinType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SelectQueryBuilderTest {

    @DisplayName("Should build select with join and group by and having")
    @Test
    void shouldBuildSelect() {
        String query = SelectQueryBuilder.from("users")
                .selectField("*")
                .join("orders", "users.id = orders.user_id", JoinType.LEFT)
                .whereCondition("age > ?")
                .groupBy("name")
                .havingCondition("COUNT(*) > 1")
                .buildSelectStatement();

        assertThat(query).isEqualTo("SELECT * FROM users LEFT JOIN orders ON users.id = orders.user_id WHERE age > ? GROUP BY name HAVING COUNT(*) > 1;");
    }

}