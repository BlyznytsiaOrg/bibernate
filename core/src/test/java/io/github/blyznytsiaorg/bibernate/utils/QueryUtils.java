package io.github.blyznytsiaorg.bibernate.utils;

import io.github.blyznytsiaorg.bibernate.session.BibernateSessionFactory;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@UtilityClass
public class QueryUtils {

    public static void assertQueries(BibernateSessionFactory bibernateSessionFactory, List<String> expectedQueries) {
        List<String> executedQueries = bibernateSessionFactory.getExecutedQueries();
        assertThat(executedQueries).isNotEmpty();
        assertThat(executedQueries.size()).isEqualTo(expectedQueries.size());

        for (int i = 0; i < expectedQueries.size(); i++) {
            assertThat(executedQueries.get(i)).isEqualTo(expectedQueries.get(i));
        }
    }

    @SneakyThrows
    public static void setupTables(DataSource dataSource, String createTable, String createInsert) {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(createTable);
            }

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(createInsert);
            }
        }
    }


}
