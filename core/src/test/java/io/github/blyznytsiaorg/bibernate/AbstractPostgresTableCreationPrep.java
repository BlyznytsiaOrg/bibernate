package io.github.blyznytsiaorg.bibernate;

public interface AbstractPostgresTableCreationPrep {

    String CREATE_PERSONS_TABLE = """
            CREATE TABLE IF NOT EXISTS persons (
                id bigserial primary key,
                first_name varchar(255),
                last_name varchar(255)
            );
            """;

    String CREATE_USERS_ADDRESSES_HOUSES_TABLES = """
            CREATE TABLE IF NOT EXISTS houses
            (
                id      BIGINT primary key,
                name    VARCHAR(255)
            );
            
            CREATE TABLE IF NOT EXISTS addresses
            (
                id      BIGINT primary key,
                name    VARCHAR(255),
                house_id BIGINT NOT NULL,
                CONSTRAINT addresses_houses_FK FOREIGN KEY (house_id) REFERENCES houses
            );
            
            CREATE TABLE IF NOT EXISTS users (
                id bigserial primary key,
                first_name varchar(255),
                last_name varchar(255),
                address_id bigint not null,
                CONSTRAINT users_addresses_FK FOREIGN KEY (address_id) REFERENCES addresses
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

    String CREATE_INSERT_USERS_ADRESSES_STATEMENT = """
            insert into houses(id, name) values (1, 'big');
            insert into addresses(id, name, house_id) values (1, 'street', 1);
            insert into users(first_name, last_name, address_id) values ('FirstName', 'LastName', 1)
            """;
            
    String CREATE_NOTES_TABLE = """
            CREATE TABLE IF NOT EXISTS notes (
                id bigserial primary key,
                person_id bigint not null,
                text varchar(255),
                CONSTRAINT notes_persons_FK FOREIGN KEY (person_id) REFERENCES persons
            );
            """;

    String CREATE_INSERT_NOTES_STATEMENT = """
            insert into notes(person_id, text) values (1, 'My First Note');
            insert into notes(person_id, text) values (1, 'My Second Note');
            """;

    String CREATE_DELETE_NOTES_STATEMENT = """
            delete from notes where person_id = 1;
            """;
}
