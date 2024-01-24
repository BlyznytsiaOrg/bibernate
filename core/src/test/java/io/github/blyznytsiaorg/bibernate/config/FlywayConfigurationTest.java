package io.github.blyznytsiaorg.bibernate.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

class FlywayConfigurationTest extends AbstractPostgresInfrastructurePrep {

    public static final String TABLE_NAME_PERSONS = "persons";

    @Test
    @DisplayName("Flyway should create table 'persons'")
    @SneakyThrows
    void shouldCreateTables() {

        //when
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select table_name from information_schema.tables");
        List<String> tableNames = new ArrayList<>();
        while (resultSet.next()) {
            String tableName = resultSet.getString(1);
            tableNames.add(tableName);
        }

        //then
        assertThat(tableNames).contains(TABLE_NAME_PERSONS);

    }

}