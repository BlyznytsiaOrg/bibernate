package io.github.blyznytsiaorg.bibernate.ddl;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
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
        createPersistentWithBb2ddlCreate("testdata.entity");

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

            }
        }
    }

    @Test
    @DisplayName("Bibernate should create table Authors")
    @SneakyThrows
    void shouldCreateTableAuthors() {
        createPersistentWithBb2ddlCreate("testdata.entity");

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
                assertThat(tableNames).contains(TABLE_NAME_AUTHORS);


                DatabaseMetaData metaData = connection.getMetaData();

                // primary key info
                String columnName = "";
                ResultSet primaryKeyResultSet = metaData.getPrimaryKeys(null, null, TABLE_NAME_AUTHORS);
                while (primaryKeyResultSet.next()) {
                    columnName = primaryKeyResultSet.getString("COLUMN_NAME");
                }
                assertThat(columnName).isEqualTo("id");

                // index info
                ResultSet indexInfoResultSet = metaData.getIndexInfo(null, null, TABLE_NAME_AUTHORS, false, false);
                List<IndexData> indexData = new ArrayList<>();
                while (indexInfoResultSet.next()) {
                    String indexName = indexInfoResultSet.getString("INDEX_NAME");
                    String columnNameWithIndex = indexInfoResultSet.getString("COLUMN_NAME");
                    boolean nonUnique = indexInfoResultSet.getBoolean("NON_UNIQUE");
                    indexData.add(new IndexData(indexName, columnNameWithIndex, nonUnique));
                }

                assertThat(indexData).hasSize(2);
                assertThat(indexData.stream().map(IndexData::column).toList())
                        .contains("id")
                        .contains("name");

                indexData.stream()
                        .filter(data -> data.column().equals("id"))
                        .forEach(data -> assertThat(data.nonUnique()).isFalse());

                indexData.stream()
                        .filter(data -> data.column().equals("name"))
                        .forEach(data -> assertThat(data.nonUnique()).isTrue());


                // foreign key info
                ResultSet importedKeysResultSet = metaData.getImportedKeys(null, null, TABLE_NAME_AUTHORS);
                List<String> foreignKeyNames = new ArrayList<>();
                while (importedKeysResultSet.next()) {
                    String foreignKeyColumnName = importedKeysResultSet.getString("FKCOLUMN_NAME");
                    foreignKeyNames.add(foreignKeyColumnName);
                }

                assertThat(foreignKeyNames).isEmpty();

                ResultSet columnResultSet = metaData.getColumns(null, null,
                        TABLE_NAME_AUTHORS, null);
                List<ColumnData> columnData = new ArrayList<>();
                while (columnResultSet.next()) {
                    String name = columnResultSet.getString("COLUMN_NAME");
                    String typeName = columnResultSet.getString("TYPE_NAME");
                    String columnSize = columnResultSet.getString("COLUMN_SIZE");
                    String columnDefinition = columnResultSet.getString("COLUMN_DEF");
                    boolean isAutoincrement = columnResultSet.getBoolean("IS_AUTOINCREMENT");
                    boolean isNullable = columnResultSet.getBoolean("IS_NULLABLE");
                    columnData.add(new ColumnData(name, typeName, columnSize,
                            columnDefinition, isAutoincrement, isNullable));

                }
                columnData.forEach(System.out::println);


            }
        }
    }
}

record ColumnData(String name, String type, String size, String definition,
                  boolean autoIncrement, boolean nullable) {

}

record IndexData(String name, String column, boolean nonUnique) {
}
