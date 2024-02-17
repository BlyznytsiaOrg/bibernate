# Transaction Management

Offers built-in support for managing database transactions, ensuring data integrity and consistency across multiple operations.

In Bibernate we have Transaction class that defines the unit of work. It maintains abstraction from the transaction 
implementation (JTA,JDBC). A representation of a database transaction that manages a connection, tracks updated entities, 
and supports transactional operations such as starting, committing, and rolling back.
Instances of this class are typically used to group a set of database operations into a single atomic unit.
A transaction is associated with BibernateSession and instantiated by calling bibernateSession.startTransaction().
The design anticipates having, at most, one uncommitted Transaction associated with a specific BibernateSession concurrently.

The methods of Transaction interface are as follows:
1. **start()** - Starts the transaction by setting auto-commit to false on the associated database connection.
2. **commit()** - Commits the transaction, and end the unit of work, applies changes to the database, and closes the associated connection.
   Clears the set of updated entities after a successful commit.
3. **rollback()** - Force the underlying transaction to roll back, undoing changes made during the transaction,
   and closes the associated connection. Resets the ID fields of all updated entities to null during rollback.
4. **addUpdatedEntity()** - Adds an entity to the set of updated entities. Entities in this set are considered modified during the transaction.
   This set is contained here in order to be able to revert changes to the entity in the rollback case.
5. **rollbackAllIds()** - Rolls back the ID fields of all entities in the updatedEntities set by setting them to null.
   This operation is typically performed during a rollback to undo changes made during the transaction.
      


## Example of Transaction Management in Bibernate
To start transaction you need to call startTransaction() method in BibernateSession, it will set autocommit mode to false
in Connection and save transaction into ThreadLocal. Lets consider next example:

```java
@Entity
@Table(name = "persons")
@ToString
@Setter
@Getter
public class Person {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private Long id;

    private String firstName;

    @Column(name = "last_name")
    private String lastName;
}
```

```java
@Slf4j
public class BibernateTransactionDemoApplication {

    public static final String ENTITY_PACKAGE = "com.levik.bibernate.demo.transaction";

    @SneakyThrows
    public static void main(String[] args) {
        log.info("Bibernate Demo Application...");
        Persistent persistent = Persistent.withDefaultConfiguration(ENTITY_PACKAGE);

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                bibernateSession.startTransaction();
                var person = new Person();
                person.setFirstName("John");
                person.setLastName("Smith");
                bibernateSession.save(Person.class, person);
                bibernateSession.commitTransaction();
            }

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                bibernateSession.startTransaction();
                var person = new Person();
                person.setFirstName("Yevgen");
                person.setLastName("P");

                bibernateSession.rollbackTransaction();
            }

            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                List<Person> all = bibernateSession.findAll(Person.class);
                all.stream().forEach(person -> log.info("Peson {}", person));
            }
        }
    }
}
```
In example above only one insertion into the 'person' table occurred, which aligns with expectations since the second 
operation was rolled back. Consequently, when querying with 'findAll', only one entity with the 'firstName' 
attribute set to 'John' is retrieved.