## Native SQL Queries


Native SQL queries allow you to execute SQL queries directly against the underlying database. This provides flexibility when working with complex queries or when you need to leverage database-specific features that are not supported by the ORM framework.

you could use 3 place to use native query:

- StatelessSession
  A StatelessSession in Bibernate provides a lightweight alternative to the regular Session interface and is particularly useful for batch processing scenarios where you don't need to manage the persistence state.

- BibernateRepository
  You can incorporate native queries directly into your repository interfaces by using the @Query annotation.

- BibernateSession
  Using native queries with the BibernateSession interface allows you to execute SQL queries directly against the database.

### Benefits
- **Flexibility:** Native SQL queries allow you to write complex SQL statements tailored to specific database features or optimizations.
- **Performance:** In some cases, native SQL queries can offer better performance compared to JPQL or Criteria API queries, especially for complex queries or bulk operations.
- **Database Features:** You can leverage database-specific features or functions that are not directly supported by the ORM framework.


### Considerations
- **Portability:** Native SQL queries are database-specific and may not work across different database systems.
- **Security:** Care should be taken to prevent SQL injection vulnerabilities when using native SQL queries. Parameterized queries should be used to bind values securely.
- 
### Best Practices
- **Use sparingly:** While native SQL queries provide flexibility, they should be used judiciously to maintain the abstraction provided by the ORM framework.
- **Optimization:** Ensure that native SQL queries are optimized for performance and adhere to database best practices.