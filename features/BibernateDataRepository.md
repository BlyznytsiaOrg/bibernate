## Bibernate Data Repository

Bibernate Data Repository provides several benefits:

- **Consistency:** By defining common CRUD (Create, Read, Update, Delete) operations in the repository interface, you ensure consistency in how data is accessed and manipulated across different parts of your application.

- **Reduced Boilerplate:** The repository interface abstracts away the details of database access, reducing the amount of boilerplate code you need to write. This makes your codebase cleaner and easier to maintain.

- **Type Safety:** The repository methods are type-safe, meaning that the compiler can catch errors at compile-time rather than at runtime. This helps prevent common mistakes and improves the robustness of your code.

- **Query Methods:** The repository interface allows you to define custom query methods using method names that follow a specific naming convention. This makes it easy to write and read custom queries without having to write SQL or HQL (Hibernate Query Language) directly.

- **Dynamic Query Generation:** Query methods can be dynamically generated based on method names, allowing you to query entities by various criteria without having to write custom query code for each use case.

- **Annotation Support:** The repository interface supports annotations like @Query and @Param, allowing you to write custom SQL or HQL queries when needed, while still leveraging the benefits of the repository pattern.

Overall, using the Bibernate Data Repository promotes code reuse, improves maintainability, and enhances productivity by providing a consistent and type-safe way to interact with your database.

Below is a table summarizing the features supported by the Bibernate Data Repository:

| Feature                        | Description                                                                                                     |
|--------------------------------|-----------------------------------------------------------------------------------------------------------------|
| CRUD Operations                | Basic CRUD operations such as findById, findAll, save, update, and delete are supported.                        |
| Custom Query Methods           | Custom query methods can be defined using method names that follow a specific naming convention.                |
| Query Method Parameters        | Query methods can accept parameters annotated with `@Param`, allowing for dynamic queries with named parameters. |
| Named Query Annotations        | Custom queries can be defined using the `@Query` annotation, supporting both native SQL and HQL queries.        |
| Query Method Composition       | Query methods can be composed using keywords such as `And`, `Or`, `Like`, `Equals`, `LessThan`, `GreaterThan`, etc. |
| Derived Query Methods          | Query methods can be derived from method names, e.g., findByUsernameAndAge, findByFirstNameLike, etc.          |
| Null and Not Null Constraints | Query methods can filter entities based on null or not null values for specific fields.                         |
| Transactional Operations       | Repository methods can participate in database transactions and support transactional semantics.                |
| Batch Operations               | Batch operations for saving and deleting collections of entities are supported.                                  |

This table provides a clear overview of the supported features of the Bibernate Data 

Here are examples of basic query interfaces:

### UserRepository

- **findByEnabled:** Find users by their enabled status.
- **findByAgeLessThan:** Find users with an age less than the specified value.
- **findByAgeLessThanEqual:** Find users with an age less than or equal to the specified value.
- **findByAgeGreaterThan:** Find users with an age greater than the specified value.
- **findByAgeGreaterThanEqual:** Find users with an age greater than or equal to the specified value.
- **findByUsernameNotNull:** Find users with a non-null username.
- **findByUsernameNull:** Find users with a null username.
- **findByUsernameAndAge:** Find users by username and age.
- **countUserDuplicate:** Count duplicate users based on a specified count.

### PersonRepository

- **findByFirstNameOrLastName:** Find persons by their first name or last name.
- **findByFirstNameEquals:** Find persons with the exact first name.
- **findByFirstNameLike:** Find persons with a first name that starts with a specified value.
- **findByFirstName:** Find persons by first name using a custom JPQL query.
- **findByFirstNameAndLastName:** Find persons by first name and last name.

This provides a clearer description of the example query methods supported by the repository interfaces, including findBy methods and custom BQL/native SQL queries.

```java
public interface UserRepository extends BibernateRepository<User, Long> {

    List<User> findByEnabled(@Param("enable") boolean enabled);

    List<User> findByAgeLessThan(@Param("age") int age);

    List<User> findByAgeLessThanEqual(@Param("age") int age);

    List<User> findByAgeGreaterThan(@Param("age") int age);

    List<User> findByAgeGreaterThanEqual(@Param("age") int age);

    List<User> findByUsernameNotNull();

    List<User> findByUsernameNull();

    Optional<User> findByUsernameAndAge(@Param("username") String username, @Param("age") int age);

    @Query(value = "select count(*) from users group by username having count(username) > ?", nativeQuery = true)
    int countUserDuplicate(@Param("count") int count);
}
```

You can also define your custom query methods as shown in the example provided by `PersonCustomQueryRepository`.

If the predefined query methods don't meet your requirements, you can create custom query methods with specific logic tailored to your application's needs. In the `PersonCustomQueryRepository` example, custom query methods are defined for retrieving persons based on various criteria, such as first name, last name, and custom BQL queries.

This flexibility allows you to extend the functionality of the repository interface by incorporating domain-specific query methods, enabling you to interact with the underlying data store in a way that suits your application's requirements.


```java
public interface PersonRepository extends BibernateRepository<Person, Long>, PersonCustomQueryRepository {
    List<Person> findByFirstNameOrLastName(@Param("first_name") String firstName, @Param("last_name") String lastName);

    List<Person> findByFirstNameEquals(@Param("first_name") String fistName);

    List<Person> findByFirstNameLike(@Param("first_name") String firstNameStart);

    @Query(value = "SELECT p FROM Person p WHERE p.firstName = ?")
    List<Person> findByFirstName(@Param("first_name") String firstName);

    Person findByFirstNameAndLastName(@Param("first_name") String firstName, @Param("last_name") String lastName);
}

```

You need to define interface and implement it and all will works.

```java
public interface PersonCustomQueryRepository {

    List<Person> findMyCustomQuery();
}

```


```java
@RequiredArgsConstructor
@Slf4j
public class PersonCustomQueryRepositoryImpl implements PersonCustomQueryRepository {

    @Override
    public List<Person> findMyCustomQuery() {
        try (var bringSession = getBibernateSessionFactory().openSession()) {
            return List.of(bringSession.findById(Person.class, 1L).orElseThrow());
        }
    }
}

```

This flexibility allows you to extend the functionality of the repository interface by incorporating domain-specific query methods, enabling you to interact with the underlying data store in a way that suits your application's requirements.
This description emphasizes the flexibility provided by custom query methods and how they allow developers to tailor data access logic according to their specific application needs.


## Your own customization

Additionally, you have the option to implement your own method handling by implementing the interface SimpleRepositoryMethodHandler. 
This allows for custom handling of repository methods beyond the standard query methods provided by Bibernate.


**Note:** 

Considering time limitations, our support for BQL (Bibernate Query Language) and native queries is currently limited. 
However, our repository provides essential query methods and basic CRUD operations, which cover most common use cases effectively. 
If required, you can extend and enhance the repository interface in the future to accommodate more advanced querying functionalities.

Please note that at the moment, BQL does not support any join operations, and when using native queries, only integer data types are supported. These limitations should be taken into consideration when crafting queries in Bibernate.