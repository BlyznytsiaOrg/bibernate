## @Param Annotation

Indicates that the annotated parameter is a named parameter for a method or constructor.

### Example usage:

```java
public interface UserRepository extends BibernateRepository<User, Long> {
    User findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
}
```

In this example, the findByUsernameAndPassword method in the UserRepository 
interface is annotated with @Param for each parameter, indicating that they are named parameters. 
These named parameters are then used in the query method.

**Parameters:**
- value: Specifies the name of the parameter.