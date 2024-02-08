package io.github.blyznytsiaorg.bibernate.ddl;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

class DDLProcessorTest extends AbstractPostgresInfrastructurePrep {
    public static final String TABLE_NAME_AUTHORS = "authors";
    public static final String TABLE_NAME_AUTHOR_PROFILES = "author_profiles";
    public static final String TABLE_NAME_BOOK = "book";
    public static final String TABLE_NAME_PHONE = "phone";
    public static final String TABLE_NAME_BOOK_AUTHORS = "books_authors";
    public static final String SELECT_TABLE_NAMES = "select table_name from information_schema.tables";

    @Test
    @DisplayName("Bibernate should create tables")
    @SneakyThrows
    void shouldCreateTables() {
        createPersistentWithBb2ddlCreate();

        //when
        List<String> tableNames = new ArrayList<>();
        try (var connection = dataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                var resultSet = statement.executeQuery(SELECT_TABLE_NAMES);
                while (resultSet.next()) {
                    String tableName = resultSet.getString(1);
                    tableNames.add(tableName);
                }

                //then
                assertThat(tableNames).contains(TABLE_NAME_AUTHORS)
                        .contains(TABLE_NAME_AUTHOR_PROFILES)
                        .contains(TABLE_NAME_BOOK)
                        .contains(TABLE_NAME_PHONE)
                        .contains(TABLE_NAME_BOOK_AUTHORS);


//            DatabaseMetaData metaData = connection.getMetaData();
//
//            ResultSet rs = metaData.getColumns(null, null,
//                    TABLE_NAME_AUTHORS, null);
//            while (rs.next()) {
//                System.out.println(rs.getString("COLUMN_NAME"));
//                System.out.println(rs.getString("TYPE_NAME"));
//                System.out.println(rs.getString("COLUMN_SIZE"));
//                System.out.println(rs.getString("COLUMN_DEF"));
//                System.out.println(rs.getBoolean("IS_AUTOINCREMENT"));
//            }
//        }
            }
        }
    }
}