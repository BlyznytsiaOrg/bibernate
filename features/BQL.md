## Bibernate Query Language(BQL)


Bibernate Query Language (BQL) is a query language specifically designed for interacting with databases through the Bibernate framework. It provides a simplified and intuitive syntax for writing database queries, similar to other query languages like SQL.


**Key features of BQL include:**

- **Object-oriented Approach:** BQL is designed to work with object-oriented data models, making it easy to query and manipulate objects directly.
- **Integration with Bibernate Entities:** BQL seamlessly integrates with Bibernate entities, allowing developers to write queries directly against their domain model objects.



```java
  @Query(value = "SELECT p FROM Person p WHERE p.firstName = :first_name")
  List<Person> findByFirstName(@Param("first_name") String firstName);
```

**In this example:**

- The @Query annotation is used to specify a custom BQL query.
- :first_name is a parameter placeholder in the BQL query, which will be replaced by the actual value of the firstName parameter.
- @Param("first_name") is used to bind the firstName parameter to the :first_name placeholder in the query.

This method retrieves a list of Person entities whose first name matches the given parameter value using the specified BQL query.


**Note:** The current version of BQL is limited to basic query operations, supporting only WHERE clauses based on individual fields. More advanced operations such as joins are not supported at this time.

In practical terms, this means that BQL queries are restricted to filtering results based on specific fields of entities, without the ability to perform complex joins across multiple tables or entities.

