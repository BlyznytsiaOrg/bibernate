package io.github.blyznytsiaorg.bibernate.ddl;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.blyznytsiaorg.bibernate.AbstractPostgresInfrastructurePrep;
import io.github.blyznytsiaorg.bibernate.ddl.model.ColumnData;
import io.github.blyznytsiaorg.bibernate.ddl.model.ForeignKey;
import io.github.blyznytsiaorg.bibernate.ddl.model.IndexData;
import io.github.blyznytsiaorg.bibernate.ddl.model.TableMetadata;
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
        createPersistentWithBb2ddlCreate("testdata.ddl");


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
    @DisplayName("Bibernate should create table 'authors'")
    void shouldCreateTableAuthors() {

        //when
        TableMetadata metadata = getMetadata("testdata.ddl", TABLE_NAME_AUTHORS);
        List<String> primaryColumnNames = metadata.primaryKeyName();
        List<IndexData> indexData = metadata.indexData();
        List<ForeignKey> foreignKeys = metadata.foreignKeys();
        List<ColumnData> columnData = metadata.columnData();

        //then
        assertThat(primaryColumnNames).contains("id");

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

        assertThat(foreignKeys).isEmpty();

        columnData.stream()
                .filter(column -> column.name().equals("id"))
                .forEach(column -> {
                    assertThat(column.type()).isEqualTo("serial");
                    assertThat(column.size()).isEqualTo("10");
                    assertThat(column.autoIncrement()).isTrue();
                    assertThat(column.nullable()).isFalse();
                    assertThat(column.definition()).contains("nextval");
                });

        columnData.stream()
                .filter(column -> column.name().equals("name"))
                .forEach(column -> {
                    assertThat(column.type()).isEqualTo("varchar");
                    assertThat(column.size()).isEqualTo("255");
                    assertThat(column.autoIncrement()).isFalse();
                    assertThat(column.nullable()).isFalse();
                    assertThat(column.definition()).isNull();
                });

        columnData.stream()
                .filter(column -> column.name().equals("created_at"))
                .forEach(column -> {
                    assertThat(column.type()).isEqualTo("timestamptz");
                    assertThat(column.size()).isEqualTo("35");
                    assertThat(column.autoIncrement()).isFalse();
                    assertThat(column.nullable()).isTrue();
                    assertThat(column.definition()).isEqualTo("now()");
                });
    }

    @Test
    @DisplayName("Bibernate should create table 'author_profiles'")
    void shouldCreateTableAuthorProfile() {

        //when
        TableMetadata metadata = getMetadata("testdata.ddl", TABLE_NAME_AUTHOR_PROFILES);
        List<String> primaryColumnNames = metadata.primaryKeyName();
        List<IndexData> indexData = metadata.indexData();
        List<ForeignKey> foreignKeys = metadata.foreignKeys();
        List<ColumnData> columnData = metadata.columnData();

        //then
        assertThat(primaryColumnNames).contains("id");

        assertThat(indexData).hasSize(1);
        assertThat(indexData.stream().map(IndexData::column).toList())
                .contains("id");

        indexData.stream()
                .filter(data -> data.column().equals("id"))
                .forEach(data -> assertThat(data.nonUnique()).isFalse());

        assertThat(foreignKeys).hasSize(1);
        foreignKeys.stream()
                .filter(fk -> fk.columnName().equals("author_id"))
                .forEach(fk -> assertThat(fk.name()).startsWithIgnoringCase("FK_"));

        columnData.stream()
                .filter(column -> column.name().equals("id"))
                .forEach(column -> {
                    assertThat(column.type()).isEqualTo("bigserial");
                    assertThat(column.size()).isEqualTo("19");
                    assertThat(column.autoIncrement()).isTrue();
                    assertThat(column.nullable()).isFalse();
                    assertThat(column.definition()).contains("nextval");
                });

        columnData.stream()
                .filter(column -> column.name().equals("about_me"))
                .forEach(column -> {
                    assertThat(column.type()).isEqualTo("text");
                    assertThat(column.size()).isEqualTo("2147483647");
                    assertThat(column.autoIncrement()).isFalse();
                    assertThat(column.nullable()).isTrue();
                    assertThat(column.definition()).isNull();
                });

        columnData.stream()
                .filter(column -> column.name().equals("author_id"))
                .forEach(column -> {
                    assertThat(column.type()).contains("int");
                    assertThat(column.size()).isEqualTo("10");
                    assertThat(column.autoIncrement()).isFalse();
                    assertThat(column.nullable()).isTrue();
                    assertThat(column.definition()).isNull();
                });
    }

    @Test
    @DisplayName("Bibernate should create table 'book'")
    void shouldCreateTableBook() {

        //when
        TableMetadata metadata = getMetadata("testdata.ddl", TABLE_NAME_BOOK);
        List<String> primaryColumnNames = metadata.primaryKeyName();
        List<IndexData> indexData = metadata.indexData();
        List<ForeignKey> foreignKeys = metadata.foreignKeys();
        List<ColumnData> columnData = metadata.columnData();

        //then
        assertThat(primaryColumnNames).contains("id");

        assertThat(indexData).hasSize(1);
        assertThat(indexData.stream().map(IndexData::column).toList())
                .contains("id");

        indexData.stream()
                .filter(data -> data.column().equals("id"))
                .forEach(data -> assertThat(data.nonUnique()).isFalse());

        assertThat(foreignKeys).isEmpty();

        columnData.forEach(System.out::println);

        columnData.stream()
                .filter(column -> column.name().equals("id"))
                .forEach(column -> {
                    assertThat(column.type()).contains("int");
                    assertThat(column.size()).isEqualTo("19");
                    assertThat(column.autoIncrement()).isFalse();
                    assertThat(column.nullable()).isFalse();
                    assertThat(column.definition()).isNull();
                });
    }

    @Test
    @DisplayName("Bibernate should create table 'phone'")
    void shouldCreateTablePhone() {

        //when
        TableMetadata metadata = getMetadata("testdata.ddl", TABLE_NAME_PHONE);
        List<String> primaryColumnNames = metadata.primaryKeyName();
        List<IndexData> indexData = metadata.indexData();
        List<ForeignKey> foreignKeys = metadata.foreignKeys();
        List<ColumnData> columnData = metadata.columnData();

        //then
        assertThat(primaryColumnNames).contains("id");

        System.out.println(indexData);
        assertThat(indexData).hasSize(3);
        assertThat(indexData.stream().map(IndexData::column).toList())
                .contains("id").contains("mobile_number").contains("company_number");

        indexData.stream()
                .filter(data -> data.column().equals("id"))
                .forEach(data -> assertThat(data.nonUnique()).isFalse());

        indexData.stream()
                .filter(data -> data.column().equals("mobile_number"))
                .forEach(data -> assertThat(data.nonUnique()).isFalse());

        assertThat(foreignKeys).hasSize(1);
        foreignKeys.stream()
                .filter(fk -> fk.columnName().equals("author_profile_id"))
                .forEach(fk -> assertThat(fk.name()).isEqualToIgnoringCase("FK_phone_author_profile"));

        columnData.stream()
                .filter(column -> column.name().equals("id"))
                .forEach(column -> {
                    assertThat(column.type()).contains("int");
                    assertThat(column.size()).isEqualTo("19");
                    assertThat(column.autoIncrement()).isFalse();
                    assertThat(column.nullable()).isFalse();
                    assertThat(column.definition()).isNull();
                });

        columnData.stream()
                .filter(column -> column.name().equals("mobile_number"))
                .forEach(column -> {
                    assertThat(column.type()).isEqualTo("varchar");
                    assertThat(column.size()).isEqualTo("255");
                    assertThat(column.autoIncrement()).isFalse();
                    assertThat(column.nullable()).isFalse();
                    assertThat(column.definition()).isNull();
                });

        columnData.stream()
                .filter(column -> column.name().equals("company_number"))
                .forEach(column -> {
                    assertThat(column.type()).isEqualTo("varchar");
                    assertThat(column.size()).isEqualTo("255");
                    assertThat(column.autoIncrement()).isFalse();
                    assertThat(column.nullable()).isTrue();
                    assertThat(column.definition()).isNull();
                });

        columnData.stream()
                .filter(column -> column.name().equals("author_profile_id"))
                .forEach(column -> {
                    assertThat(column.type()).contains("int");
                    assertThat(column.size()).isEqualTo("19");
                    assertThat(column.autoIncrement()).isFalse();
                    assertThat(column.nullable()).isTrue();
                    assertThat(column.definition()).isNull();
                });
    }

    @Test
    @DisplayName("Bibernate should create table 'books_authors'")
    void shouldCreateTableBookAuthors() {

        //when
        TableMetadata metadata = getMetadata("testdata.ddl", TABLE_NAME_BOOK_AUTHORS);
        List<String> primaryColumnNames = metadata.primaryKeyName();
        List<IndexData> indexData = metadata.indexData();
        List<ForeignKey> foreignKeys = metadata.foreignKeys();
        List<ColumnData> columnData = metadata.columnData();
        System.out.println(primaryColumnNames);
        System.out.println(indexData);
        System.out.println(foreignKeys);
        System.out.println(columnData);


        //then
        assertThat(primaryColumnNames).contains("book_id").contains("author_id");

        System.out.println(indexData);
        assertThat(indexData).hasSize(2);
        assertThat(indexData.stream().map(IndexData::column).toList())
                .contains("book_id").contains("author_id");

        indexData.stream()
                .filter(data -> data.column().equals("book_id"))
                .forEach(data -> assertThat(data.nonUnique()).isFalse());

        indexData.stream()
                .filter(data -> data.column().equals("author_id"))
                .forEach(data -> assertThat(data.nonUnique()).isFalse());

        assertThat(foreignKeys).hasSize(2);
        foreignKeys.stream()
                .filter(fk -> fk.columnName().equals("book_id"))
                .forEach(fk -> assertThat(fk.name()).isEqualToIgnoringCase("FK_book_book_authors"));

        foreignKeys.stream()
                .filter(fk -> fk.columnName().equals("author_id"))
                .forEach(fk -> assertThat(fk.name()).isEqualToIgnoringCase("FK_authors_book_authors"));

        columnData.forEach(System.out::println);

        columnData.stream()
                .filter(column -> column.name().equals("book_id"))
                .forEach(column -> {
                    assertThat(column.type()).contains("int");
                    assertThat(column.size()).isEqualTo("19");
                    assertThat(column.autoIncrement()).isFalse();
                    assertThat(column.nullable()).isFalse();
                    assertThat(column.definition()).isNull();
                });

        columnData.stream()
                .filter(column -> column.name().equals("author_id"))
                .forEach(column -> {
                    assertThat(column.type()).contains("int");
                    assertThat(column.size()).isEqualTo("10");
                    assertThat(column.autoIncrement()).isFalse();
                    assertThat(column.nullable()).isFalse();
                    assertThat(column.definition()).isNull();
                });
    }

    @SneakyThrows
    private TableMetadata getMetadata(String packageName, String tableName) {

        createPersistentWithBb2ddlCreate(packageName);

        try (var connection = dataSource.getConnection()) {

            DatabaseMetaData metaData = connection.getMetaData();

            // primary key info
            String primaryKeyColumnName = "";
            ResultSet primaryKeyResultSet = metaData.getPrimaryKeys(null, null, tableName);
            List<String> primaryKeyColumnNames = new ArrayList<>();
            while (primaryKeyResultSet.next()) {
                primaryKeyColumnName = primaryKeyResultSet.getString("COLUMN_NAME");
                primaryKeyColumnNames.add(primaryKeyColumnName);
            }

            // index info
            ResultSet indexInfoResultSet = metaData.getIndexInfo(null, null, tableName, false, false);
            List<IndexData> indexData = new ArrayList<>();
            while (indexInfoResultSet.next()) {
                String indexName = indexInfoResultSet.getString("INDEX_NAME");
                String columnNameWithIndex = indexInfoResultSet.getString("COLUMN_NAME");
                boolean nonUnique = indexInfoResultSet.getBoolean("NON_UNIQUE");
                indexData.add(new IndexData(indexName, columnNameWithIndex, nonUnique));
            }

            // foreign key info
            ResultSet importedKeysResultSet = metaData.getImportedKeys(null, null, tableName);
            List<ForeignKey> foreignKeyColumnNames = new ArrayList<>();
            while (importedKeysResultSet.next()) {
                String foreignKeyColumnName = importedKeysResultSet.getString("FKCOLUMN_NAME");
                String foreignKeyName = importedKeysResultSet.getString("FK_NAME");
                ForeignKey foreignKey = new ForeignKey(foreignKeyColumnName, foreignKeyName);
                foreignKeyColumnNames.add(foreignKey);
            }

            //column info
            ResultSet columnResultSet = metaData.getColumns(null, null,
                    tableName, null);
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
            return new TableMetadata(primaryKeyColumnNames,
                    indexData, foreignKeyColumnNames, columnData);
        }
    }
}

