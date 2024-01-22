package io.github.blyznytsiaorg.bibernate;

public interface AbstractPostgresTableCreationPrep {

    String CREATE_PERSONS_TABLE = """
            CREATE TABLE IF NOT EXISTS persons (
                id bigserial primary key,
                first_name varchar(255),
                last_name varchar(255)
            );
            """;

    String CREATE_USERS_ADDRESSES_TABLES = """
            CREATE TABLE IF NOT EXISTS addresses
            (
                id      BIGINT primary key,
                name    VARCHAR(255)
            );
            
            CREATE TABLE IF NOT EXISTS users (
                id bigserial primary key,
                first_name varchar(255),
                last_name varchar(255),
                address_id bigint not null,
                CONSTRAINT users_addresses_FK FOREIGN KEY (address_id) REFERENCES addresses
            );
            """;

    String CREATE_INSERT_STATEMENT = """
            insert into persons(first_name, last_name) values ('FirstName', 'LastName');
            """;

    String CREATE_INSERT_USERS_ADRESSES_STATEMENT = """
            insert into addresses(id, name) values (1, 'street');
            insert into users(first_name, last_name, address_id) values ('FirstName', 'LastName', 1);
            """;
}
