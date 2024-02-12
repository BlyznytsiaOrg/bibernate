package io.github.blyznytsiaorg.bibernate;

public interface AbstractPostgresTableCreationPrep {

    String CREATE_EMPLOYEE_TABLE = """
            CREATE TABLE IF NOT EXISTS employees (
                id bigserial primary key,
                first_name varchar(255),
                last_name varchar(255),
                version int
            );
            """;

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
                houses_id      BIGINT primary key,
                houses_name    VARCHAR(255)
            );
                        
            CREATE TABLE IF NOT EXISTS addresses
            (
                addresses_id      BIGINT primary key,
                addresses_name    VARCHAR(255)
            );
                        
            CREATE TABLE IF NOT EXISTS users (
                users_id bigserial primary key,
                users_first_name varchar(255),
                users_last_name varchar(255),
                users_address_id bigint not null,
                users_house_id bigint not null,
                CONSTRAINT users_addresses_FK FOREIGN KEY (users_address_id) REFERENCES addresses,
                CONSTRAINT users_houses_FK FOREIGN KEY (users_house_id) REFERENCES houses
            );
            """;

    String CREATE_USERS_ADDRESSES_FOR_BI_TABLES = """                   
            CREATE TABLE IF NOT EXISTS addresses
            (
                addresses_id      BIGINT primary key,
                addresses_name    VARCHAR(255)
            );
                        
            CREATE TABLE IF NOT EXISTS users (
                users_id bigserial primary key,
                users_first_name varchar(255),
                users_last_name varchar(255),
                users_address_id bigint not null,
                CONSTRAINT users_addresses_FK FOREIGN KEY (users_address_id) REFERENCES addresses
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

    String ONE_TO_ONE_MULTIRELATION_TABLES = """
            CREATE TABLE houses (
                houses_id BIGINT PRIMARY KEY,
                houses_name VARCHAR(255)
            );
                        
            CREATE TABLE addresses (
                addresses_id BIGINT PRIMARY KEY,
                addresses_name VARCHAR(255),
                addresses_house_id BIGINT,
                FOREIGN KEY (addresses_house_id) REFERENCES houses(houses_id)
            );
                        
            CREATE TABLE users (
                users_id BIGINT PRIMARY KEY,
                users_first_name VARCHAR(255),
                users_last_name VARCHAR(255),
                users_address_id BIGINT,
                FOREIGN KEY (users_address_id) REFERENCES addresses(addresses_id)
            );
            """;

    String ONE_TO_ONE_MULTIRELATION_INSERT = """
            INSERT INTO houses (houses_id, houses_name) VALUES
            (1, 'House A'),
            (2, 'House B');
                        
            INSERT INTO addresses (addresses_id, addresses_name, addresses_house_id) VALUES
            (1, 'Address 1', 1),
            (2, 'Address 2', 2);
                        
            INSERT INTO users (users_id, users_first_name, users_last_name, users_address_id) VALUES
            (1, 'John', 'Doe', 1),
            (2, 'Jane', 'Smith', 2);
            """;

    String CREATE_EMPLOYEE_INSERT_STATEMENT = """
            insert into employees(id, first_name, last_name, version) values (10, 'Levik', 'P', 1);
            """;

    String CREATE_EMPLOYEE_GENERAL_INSERT_STATEMENT = """
            insert into employees(first_name, last_name, version) values ('%s', '%s', %d);
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
            insert into houses(houses_id, houses_name) values (2, 'big');
            insert into addresses(addresses_id, addresses_name) values (1, 'street');
            insert into users(users_first_name, users_last_name, users_address_id, users_house_id) values ('FirstName', 'LastName', 1, 2)
            """;

    String CREATE_INSERT_USERS_ADRESSES_FOR_BI_STATEMENT = """
            insert into addresses(addresses_id, addresses_name) values (2, 'street');
            insert into users(users_first_name, users_last_name, users_address_id) values ('FirstName', 'LastName', 2)
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

    String CREATE_INSERT_NOTE_STATEMENT = """
            insert into notes(person_id, text) values (1, 'My First Note');
            """;

    String CREATE_DELETE_NOTES_STATEMENT = """
            delete from notes where person_id = 1;
            """;

    String CREATE_PERSONS_COURSES_TABLES = """
            CREATE TABLE houses (
                house_id bigserial PRIMARY KEY,
                name VARCHAR(255)
            );
                        
            CREATE TABLE addresses (
                address_id bigserial PRIMARY KEY,
                address VARCHAR(255),
                address_house_id BIGINT,
                CONSTRAINT address_house_FK FOREIGN KEY (address_house_id) REFERENCES houses
            );
                        
            CREATE TABLE IF NOT EXISTS persons (
                id bigserial primary key,
                first_name varchar(255),
                last_name varchar(255),
                person_address_id BIGINT,
                CONSTRAINT person_address_FK FOREIGN KEY (person_address_id) REFERENCES addresses
            );
                        
            CREATE TABLE IF NOT EXISTS authors (
                id bigserial primary key,
                name varchar(255)
            );
                        
            CREATE TABLE IF NOT EXISTS courses (
                id bigserial primary key,
                author_id bigint,
                name varchar(255),
                CONSTRAINT courses_author_FK FOREIGN KEY (author_id) REFERENCES authors
            );
                        
            CREATE TABLE IF NOT EXISTS persons_courses (
                id bigserial primary key,
                person_id bigint,
                course_id bigint,
                CONSTRAINT persons_courses_persons_FK FOREIGN KEY (person_id) REFERENCES persons,
                CONSTRAINT persons_courses_courses_FK FOREIGN KEY (course_id) REFERENCES courses
            );
            """;

    String CREATE_INSERT_PERSONS_COURSES_STATEMENTS = """
            insert into houses(name) values ('house1');
            insert into houses(name) values ('house2');
            insert into houses(name) values ('house3');
            insert into addresses(address, address_house_id) values ('street1', 1);
            insert into addresses(address, address_house_id) values ('street2', 2);
            insert into addresses(address, address_house_id) values ('street3', 3);
            insert into persons(first_name, last_name, person_address_id) values ('John', 'Doe', 1);
            insert into persons(first_name, last_name, person_address_id) values ('Jordan', 'Rodriguez', 2);
            insert into persons(first_name, last_name, person_address_id) values ('Ava', 'Mitchell', 3);
            insert into authors(name) values ('Bobocode');
            insert into courses(author_id, name) values (1, 'Bobocode 2.0');
            insert into courses(author_id, name) values (1, 'Bobocode 3.0');
            insert into persons_courses(person_id, course_id) values (1, 1);
            insert into persons_courses(person_id, course_id) values (1, 2);
            insert into persons_courses(person_id, course_id) values (2, 1);
            insert into persons_courses(person_id, course_id) values (3, 2);
            """;

    String CREATE_PERSON_ID_SEQUENCE = "create sequence if not exists persons_id_seq start with 1 increment by 1";

    String CREATE_PERSON_ID_CUSTOM_SEQ = "create sequence if not exists person_id_custom_seq minvalue 6 start 6 increment by 5";

}
