package io.github.blyznytsiaorg.bibernate;

public interface AbstractPostgresTableCreationPrep {

    String CREATE_PERSONS_TABLE = """
            CREATE TABLE IF NOT EXISTS persons (
                id bigserial primary key,
                first_name varchar(255),
                last_name varchar(255)
            );
            """;

    String CREATE_USERS_TABLE = """
            CREATE TABLE IF NOT EXISTS users (
                id bigserial primary key,
                username varchar(255),
                enabled bool,
                age int
            );
            """;

    String CREATE_PERSONS_INSERT_STATEMENT = """
            insert into persons(first_name, last_name) values ('FirstName', 'LastName');
            """;

    String CREATE_PERSONS_GENERAL_INSERT_STATEMENT = """
            insert into persons(first_name, last_name) values ('%s', '%s');
            """;

    String CREATE_USERS_GENERAL_INSERT_STATEMENT = """
            insert into users(username, enabled, age) values ('%s', %s, %s);
            """;

    String CREATE_USERS_WITH_NULL_USERNAME_INSERT_STATEMENT = """
            insert into users(username, enabled, age) values (null, %s, %s);
            """;
}
