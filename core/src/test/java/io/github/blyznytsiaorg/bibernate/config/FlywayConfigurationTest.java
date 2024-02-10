package io.github.blyznytsiaorg.bibernate.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

class FlywayConfigurationTest extends AbstractPostgresInfrastructurePrep {

    public static final String TABLE_NAME_PERSONS = "persons";
    public static final String SELECT_TABLE_NAMES = "select table_name from information_schema.tables";

    @Test
    @DisplayName("Flyway should create table 'persons'")
    @SneakyThrows
    void shouldCreateTables() {
        createPersistentWithFlayWayEnabled("testdata.config");

        //when
        List<String> tableNames = new ArrayList<>();
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            var resultSet = statement.executeQuery(SELECT_TABLE_NAMES);
            while (resultSet.next()) {
                String tableName = resultSet.getString(1);
                tableNames.add(tableName);
            }
        }

        //then
        assertThat(tableNames).contains(TABLE_NAME_PERSONS);

    }

}