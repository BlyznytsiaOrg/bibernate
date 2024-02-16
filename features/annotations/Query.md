## @Query Annotation

Indicates that the annotated method represents a query.

### Example usage:

```java
public interface UserRepository extends BibernateRepository<User, Long> {
    @Query(value = "select count(*) from users group by username having count(username) > ?", nativeQuery = true)
    int countUserDuplicate(@Param("count") int count);
}
```


**Description:**
This interface defines a method countUserDuplicate, which uses the @Query annotation to specify a native SQL query. The query counts the number of duplicate users based on the number of occurrences of their usernames in the users table. The count parameter specifies the threshold for considering a user as a duplicate.

**Parameters:**

- value: Specifies the SQL query to be executed.
- nativeQuery: Indicates that the query is a native SQL query.