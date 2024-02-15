## Extensibility

You have the flexibility to extend the capabilities of your Bibernate Repository by incorporating custom method handlers. 
This empowers your repository with additional functionalities tailored to your specific needs, seamlessly integrated into your existing setup.


```java
public class UserRepositoryMethodHandler implements SimpleRepositoryMethodHandler {

    // Determines if the method should be handled by this handler
    public boolean isMethodHandle(Method method) {
        return method.getName().equals("<mathodNmae>");
    }

    // Executes the custom logic for the handled method
    public Object execute(Method method, Object[] parameters, RepositoryDetails repositoryDetails,
                          MethodMetadata methodMetadata) {
        // Custom implementation to retrieve entity by primary key from the database
    }
}
```

**Note:** Ensure to place your custom implementation within the io.github.blyznytsiaorg.bibernate package.

With this approach, Bibernate seamlessly integrates your custom method handlers, enhancing the functionality of your Hibernate Repository without compromising on simplicity or efficiency.


You can define interface and implement it and all will work.

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

